package com.teilon.financeiro.service;

import com.teilon.financeiro.dto.AlertaMetaResponse;
import com.teilon.financeiro.dto.MetaRequest;
import com.teilon.financeiro.dto.MetaResponse;
import com.teilon.financeiro.model.Meta;
import com.teilon.financeiro.repository.LancamentoRepository;
import com.teilon.financeiro.repository.MetaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MetaService {

    private final MetaRepository metaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final CategoriaService categoriaService;
    private final UsuarioService usuarioService;

    public List<MetaResponse> listar() {
        var usuario = usuarioService.getAutenticado();
        return metaRepository.findAllByUsuario(usuario).stream()
                .map(MetaResponse::de)
                .toList();
    }

    public MetaResponse criar(MetaRequest request) {
        var usuario = usuarioService.getAutenticado();
        var categoria = categoriaService.buscarPorIdEUsuario(request.categoriaId());

        if (metaRepository.existsByCategoriaAndMesAndAnoAndUsuario(categoria, request.mes(), request.ano(), usuario)) {
            throw new IllegalStateException(
                    "Já existe uma meta para a categoria '%s' em %02d/%d"
                            .formatted(categoria.getNome(), request.mes(), request.ano()));
        }

        var meta = Meta.builder()
                .categoria(categoria)
                .valorLimite(request.valorLimite())
                .mes(request.mes())
                .ano(request.ano())
                .usuario(usuario)
                .build();

        return MetaResponse.de(metaRepository.save(meta));
    }

    public void deletar(Long id) {
        var usuario = usuarioService.getAutenticado();
        var meta = metaRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new EntityNotFoundException("Meta não encontrada"));
        metaRepository.delete(meta);
    }

    public List<AlertaMetaResponse> alertas(Integer mes, Integer ano) {
        var usuario = usuarioService.getAutenticado();
        var hoje = LocalDate.now();
        int m = mes != null ? mes : hoje.getMonthValue();
        int a = ano != null ? ano : hoje.getYear();

        return metaRepository.findAllByUsuario(usuario).stream()
                .filter(meta -> meta.getMes() == m && meta.getAno() == a)
                .flatMap(meta -> {
                    var totalGasto = lancamentoRepository.sumByUsuarioAndCategoriaAndMesAndAno(
                            usuario, meta.getCategoria(), m, a);
                    if (totalGasto.compareTo(meta.getValorLimite()) <= 0) {
                        return java.util.stream.Stream.empty();
                    }
                    return java.util.stream.Stream.of(new AlertaMetaResponse(
                            meta.getId(),
                            meta.getCategoria().getId(),
                            meta.getCategoria().getNome(),
                            meta.getMes(),
                            meta.getAno(),
                            meta.getValorLimite(),
                            totalGasto,
                            totalGasto.subtract(meta.getValorLimite())
                    ));
                })
                .toList();
    }
}
