package com.teilon.financeiro.service;

import com.teilon.financeiro.dto.OrcamentoResponse;
import com.teilon.financeiro.model.*;
import com.teilon.financeiro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrcamentoService {

    private final OrcamentoMensalRepository orcamentoRepository;
    private final CategoriaRepository categoriaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final UsuarioService usuarioService;

    public List<OrcamentoResponse> listar(int mes, int ano) {
        Usuario usuario = usuarioService.getAutenticado();
        return orcamentoRepository.findAllByUsuarioAndMesAndAno(usuario, mes, ano)
                .stream()
                .map(o -> toResponse(o, usuario, mes, ano))
                .toList();
    }

    @Transactional
    public OrcamentoResponse salvar(Long categoriaId, int mes, int ano, BigDecimal valorLimite) {
        Usuario usuario = usuarioService.getAutenticado();
        Categoria categoria = categoriaRepository.findByIdAndUsuario(categoriaId, usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));

        // Upsert: se já existe, atualiza; senão cria
        OrcamentoMensal orcamento = orcamentoRepository
                .findAllByUsuarioAndMesAndAno(usuario, mes, ano)
                .stream()
                .filter(o -> o.getCategoria().getId().equals(categoriaId))
                .findFirst()
                .orElse(OrcamentoMensal.builder().usuario(usuario).categoria(categoria).mes(mes).ano(ano).build());

        orcamento.setValorLimite(valorLimite);
        orcamento = orcamentoRepository.save(orcamento);
        return toResponse(orcamento, usuario, mes, ano);
    }

    @Transactional
    public void deletar(Long id) {
        Usuario usuario = usuarioService.getAutenticado();
        OrcamentoMensal o = orcamentoRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orçamento não encontrado"));
        orcamentoRepository.delete(o);
    }

    private OrcamentoResponse toResponse(OrcamentoMensal o, Usuario usuario, int mes, int ano) {
        BigDecimal gasto = lancamentoRepository
                .sumByUsuarioAndCategoriaAndMesAndAno(usuario, o.getCategoria(), mes, ano);
        int pct = o.getValorLimite().compareTo(BigDecimal.ZERO) == 0 ? 0 :
                gasto.multiply(BigDecimal.valueOf(100))
                        .divide(o.getValorLimite(), 0, RoundingMode.HALF_UP)
                        .intValue();
        return new OrcamentoResponse(
                o.getId(),
                o.getCategoria().getId(),
                o.getCategoria().getNome(),
                o.getCategoria().getTipo().name(),
                o.getMes(), o.getAno(),
                o.getValorLimite(), gasto, Math.min(pct, 100));
    }
}
