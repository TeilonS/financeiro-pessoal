package com.teilon.financeiro.service;

import com.teilon.financeiro.dto.LancamentoRequest;
import com.teilon.financeiro.dto.LancamentoResponse;
import com.teilon.financeiro.dto.ResumoResponse;
import com.teilon.financeiro.model.Lancamento;
import com.teilon.financeiro.model.TipoTransacao;
import com.teilon.financeiro.repository.LancamentoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LancamentoService {

    private final LancamentoRepository lancamentoRepository;
    private final CategoriaService categoriaService;
    private final UsuarioService usuarioService;

    public List<LancamentoResponse> listar(Integer mes, Integer ano, TipoTransacao tipo) {
        var usuario = usuarioService.getAutenticado();

        List<Lancamento> lancamentos;

        if (mes != null && ano != null) {
            lancamentos = tipo != null
                    ? lancamentoRepository.findAllByUsuarioAndMesAndAnoAndTipo(usuario, mes, ano, tipo)
                    : lancamentoRepository.findAllByUsuarioAndMesAndAno(usuario, mes, ano);
        } else {
            lancamentos = tipo != null
                    ? lancamentoRepository.findAllByUsuarioAndTipo(usuario, tipo)
                    : lancamentoRepository.findAllByUsuario(usuario);
        }

        return lancamentos.stream().map(LancamentoResponse::de).toList();
    }

    public LancamentoResponse criar(LancamentoRequest request) {
        var usuario = usuarioService.getAutenticado();
        var categoria = categoriaService.buscarPorIdEUsuario(request.categoriaId());

        if (categoria.getTipo() != request.tipo()) {
            throw new IllegalArgumentException(
                    "Tipo do lançamento (%s) não bate com o tipo da categoria (%s)"
                            .formatted(request.tipo(), categoria.getTipo()));
        }

        var lancamento = Lancamento.builder()
                .descricao(request.descricao())
                .valor(request.valor())
                .data(request.data())
                .tipo(request.tipo())
                .categoria(categoria)
                .usuario(usuario)
                .build();

        return LancamentoResponse.de(lancamentoRepository.save(lancamento));
    }

    public LancamentoResponse atualizar(Long id, LancamentoRequest request) {
        var usuario = usuarioService.getAutenticado();
        var lancamento = lancamentoRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new EntityNotFoundException("Lançamento não encontrado"));

        var categoria = categoriaService.buscarPorIdEUsuario(request.categoriaId());

        if (categoria.getTipo() != request.tipo()) {
            throw new IllegalArgumentException(
                    "Tipo do lançamento (%s) não bate com o tipo da categoria (%s)"
                            .formatted(request.tipo(), categoria.getTipo()));
        }

        lancamento.setDescricao(request.descricao());
        lancamento.setValor(request.valor());
        lancamento.setData(request.data());
        lancamento.setTipo(request.tipo());
        lancamento.setCategoria(categoria);

        return LancamentoResponse.de(lancamentoRepository.save(lancamento));
    }

    public void deletar(Long id) {
        var usuario = usuarioService.getAutenticado();
        var lancamento = lancamentoRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new EntityNotFoundException("Lançamento não encontrado"));
        lancamentoRepository.delete(lancamento);
    }

    public byte[] exportarCsv(Integer mes, Integer ano, TipoTransacao tipo) {
        var lancamentos = listar(mes, ano, tipo);
        var sb = new StringBuilder();
        sb.append("Data,Descricao,Tipo,Categoria,Valor\n");
        for (var l : lancamentos) {
            sb.append(l.data()).append(',')
              .append('"').append(l.descricao().replace("\"", "\"\"")).append('"').append(',')
              .append(l.tipo()).append(',')
              .append('"').append(l.categoriaNome().replace("\"", "\"\"")).append('"').append(',')
              .append(l.valor()).append('\n');
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public ResumoResponse resumo(int mes, int ano) {
        var usuario = usuarioService.getAutenticado();
        var lancamentos = lancamentoRepository.findAllByUsuarioAndMesAndAno(usuario, mes, ano);

        var totalReceitas = lancamentos.stream()
                .filter(l -> l.getTipo() == TipoTransacao.RECEITA)
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var totalDespesas = lancamentos.stream()
                .filter(l -> l.getTipo() == TipoTransacao.DESPESA)
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var breakdown = lancamentos.stream()
                .filter(l -> l.getCategoria() != null)
                .collect(Collectors.groupingBy(
                        l -> l.getCategoria().getId(),
                        Collectors.toList()
                ))
                .entrySet().stream()
                .map(entry -> {
                    var lista = entry.getValue();
                    var cat = lista.get(0).getCategoria();
                    var total = lista.stream()
                            .map(Lancamento::getValor)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new ResumoResponse.CategoriaSaldo(
                            cat.getId(), cat.getNome(), cat.getTipo().name(), total);
                })
                .toList();

        return new ResumoResponse(mes, ano, totalReceitas, totalDespesas,
                totalReceitas.subtract(totalDespesas), breakdown);
    }
}
