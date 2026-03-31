package com.teilon.financeiro.service;

import com.teilon.financeiro.model.*;
import com.teilon.financeiro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BackupService {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final MetaRepository metaRepository;
    private final OrcamentoMensalRepository orcamentoRepository;
    private final CartaoCreditoRepository cartaoRepository;
    private final RecorrenciaRepository recorrenciaRepository;

    public Map<String, Object> exportar() {
        Usuario usuario = usuarioService.getAutenticado();

        List<Map<String, Object>> categorias = categoriaRepository.findAllByUsuario(usuario).stream()
                .map(c -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", c.getId());
                    m.put("nome", c.getNome());
                    m.put("tipo", c.getTipo().name());
                    m.put("cor", c.getCor());
                    m.put("categoriaPaiId", c.getCategoriaPai() != null ? c.getCategoriaPai().getId() : null);
                    return m;
                }).toList();

        List<Map<String, Object>> lancamentos = lancamentoRepository.findAllByUsuario(usuario).stream()
                .map(l -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("descricao", l.getDescricao());
                    m.put("valor", l.getValor());
                    m.put("data", l.getData().toString());
                    m.put("tipo", l.getTipo().name());
                    m.put("categoriaId", l.getCategoria() != null ? l.getCategoria().getId() : null);
                    return m;
                }).toList();

        List<Map<String, Object>> metas = metaRepository.findAllByUsuario(usuario).stream()
                .map(meta -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("valorLimite", meta.getValorLimite());
                    m.put("mes", meta.getMes());
                    m.put("ano", meta.getAno());
                    m.put("categoriaId", meta.getCategoria().getId());
                    return m;
                }).toList();

        List<Map<String, Object>> orcamentos = orcamentoRepository.findAllByUsuario(usuario).stream()
                .map(o -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("mes", o.getMes());
                    m.put("ano", o.getAno());
                    m.put("valorLimite", o.getValorLimite());
                    m.put("categoriaId", o.getCategoria().getId());
                    return m;
                }).toList();

        List<Map<String, Object>> cartoes = cartaoRepository.findAllByUsuario(usuario).stream()
                .map(c -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("nome", c.getNome());
                    m.put("limite", c.getLimite());
                    m.put("faturaAtual", c.getFaturaAtual());
                    m.put("diaVencimento", c.getDiaVencimento());
                    m.put("cor", c.getCor());
                    return m;
                }).toList();

        List<Map<String, Object>> recorrencias = recorrenciaRepository.findAllByUsuario(usuario).stream()
                .map(r -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("descricao", r.getDescricao());
                    m.put("valor", r.getValor());
                    m.put("tipo", r.getTipo().name());
                    m.put("frequencia", r.getFrequencia().name());
                    m.put("diaReferencia", r.getDiaReferencia());
                    m.put("dataInicio", r.getDataInicio().toString());
                    m.put("dataFim", r.getDataFim() != null ? r.getDataFim().toString() : null);
                    m.put("ativa", r.getAtiva());
                    m.put("categoriaId", r.getCategoria() != null ? r.getCategoria().getId() : null);
                    return m;
                }).toList();

        Map<String, Object> backup = new LinkedHashMap<>();
        backup.put("versao", "1.0");
        backup.put("data", LocalDate.now().toString());
        Map<String, Object> usuarioMap = new LinkedHashMap<>();
        usuarioMap.put("reservaEmergencia", usuario.getReservaEmergencia());
        backup.put("usuario", usuarioMap);
        backup.put("categorias", categorias);
        backup.put("lancamentos", lancamentos);
        backup.put("metas", metas);
        backup.put("orcamentos", orcamentos);
        backup.put("cartoes", cartoes);
        backup.put("recorrencias", recorrencias);
        return backup;
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> importar(Map<String, Object> backup) {
        if (backup == null || backup.isEmpty()) {
            throw new IllegalArgumentException("Arquivo de backup vazio ou inválido");
        }
        if (!(backup.get("categorias") instanceof List) && !(backup.get("lancamentos") instanceof List)) {
            throw new IllegalArgumentException("Estrutura do backup inválida: campos 'categorias' e 'lancamentos' ausentes");
        }

        Usuario usuario = usuarioService.getAutenticado();

        Map<String, Object> usuarioData = (Map<String, Object>) backup.get("usuario");
        if (usuarioData != null && usuarioData.get("reservaEmergencia") != null) {
            usuario.setReservaEmergencia(toBD(usuarioData.get("reservaEmergencia")));
            usuarioRepository.save(usuario);
        }

        List<Map<String, Object>> categorias = (List<Map<String, Object>>) backup.getOrDefault("categorias", List.of());
        Map<Long, Long> catIdMap = new HashMap<>();

        // Pass 1: root categories
        for (Map<String, Object> c : categorias) {
            if (c.get("categoriaPaiId") == null) {
                Categoria cat = categoriaRepository.save(Categoria.builder()
                        .nome((String) c.get("nome"))
                        .tipo(TipoTransacao.valueOf((String) c.get("tipo")))
                        .cor((String) c.get("cor"))
                        .usuario(usuario)
                        .build());
                catIdMap.put(toLong(c.get("id")), cat.getId());
            }
        }
        // Pass 2: subcategories
        for (Map<String, Object> c : categorias) {
            if (c.get("categoriaPaiId") != null) {
                Long newPaiId = catIdMap.get(toLong(c.get("categoriaPaiId")));
                Categoria pai = newPaiId != null ? categoriaRepository.findById(newPaiId).orElse(null) : null;
                Categoria cat = categoriaRepository.save(Categoria.builder()
                        .nome((String) c.get("nome"))
                        .tipo(TipoTransacao.valueOf((String) c.get("tipo")))
                        .cor((String) c.get("cor"))
                        .categoriaPai(pai)
                        .usuario(usuario)
                        .build());
                catIdMap.put(toLong(c.get("id")), cat.getId());
            }
        }

        List<Map<String, Object>> lancamentos = (List<Map<String, Object>>) backup.getOrDefault("lancamentos", List.of());
        int lancCount = 0;
        for (Map<String, Object> l : lancamentos) {
            Long catId = catIdMap.get(toLong(l.get("categoriaId")));
            if (catId == null) continue;
            Optional<Categoria> cat = categoriaRepository.findById(catId);
            if (cat.isEmpty()) continue;
            lancamentoRepository.save(Lancamento.builder()
                    .descricao((String) l.get("descricao"))
                    .valor(toBD(l.get("valor")))
                    .data(LocalDate.parse((String) l.get("data")))
                    .tipo(TipoTransacao.valueOf((String) l.get("tipo")))
                    .categoria(cat.get())
                    .usuario(usuario)
                    .build());
            lancCount++;
        }

        List<Map<String, Object>> metas = (List<Map<String, Object>>) backup.getOrDefault("metas", List.of());
        int metaCount = 0;
        for (Map<String, Object> meta : metas) {
            Long catId = catIdMap.get(toLong(meta.get("categoriaId")));
            if (catId == null) continue;
            Optional<Categoria> cat = categoriaRepository.findById(catId);
            if (cat.isEmpty()) continue;
            metaRepository.save(Meta.builder()
                    .valorLimite(toBD(meta.get("valorLimite")))
                    .mes(toInt(meta.get("mes")))
                    .ano(toInt(meta.get("ano")))
                    .categoria(cat.get())
                    .usuario(usuario)
                    .build());
            metaCount++;
        }

        List<Map<String, Object>> orcamentos = (List<Map<String, Object>>) backup.getOrDefault("orcamentos", List.of());
        int orcCount = 0;
        for (Map<String, Object> orc : orcamentos) {
            Long catId = catIdMap.get(toLong(orc.get("categoriaId")));
            if (catId == null) continue;
            Optional<Categoria> cat = categoriaRepository.findById(catId);
            if (cat.isEmpty()) continue;
            orcamentoRepository.save(OrcamentoMensal.builder()
                    .mes(toInt(orc.get("mes")))
                    .ano(toInt(orc.get("ano")))
                    .valorLimite(toBD(orc.get("valorLimite")))
                    .categoria(cat.get())
                    .usuario(usuario)
                    .build());
            orcCount++;
        }

        List<Map<String, Object>> cartoes = (List<Map<String, Object>>) backup.getOrDefault("cartoes", List.of());
        for (Map<String, Object> c : cartoes) {
            cartaoRepository.save(CartaoCredito.builder()
                    .nome((String) c.get("nome"))
                    .limite(toBD(c.get("limite")))
                    .faturaAtual(toBD(c.getOrDefault("faturaAtual", BigDecimal.ZERO)))
                    .diaVencimento(toInt(c.get("diaVencimento")))
                    .cor((String) c.getOrDefault("cor", "#6366f1"))
                    .usuario(usuario)
                    .build());
        }

        List<Map<String, Object>> recorrencias = (List<Map<String, Object>>) backup.getOrDefault("recorrencias", List.of());
        int recCount = 0;
        for (Map<String, Object> r : recorrencias) {
            Long catId = catIdMap.get(toLong(r.get("categoriaId")));
            if (catId == null) continue;
            Optional<Categoria> cat = categoriaRepository.findById(catId);
            if (cat.isEmpty()) continue;
            recorrenciaRepository.save(Recorrencia.builder()
                    .descricao((String) r.get("descricao"))
                    .valor(toBD(r.get("valor")))
                    .tipo(TipoTransacao.valueOf((String) r.get("tipo")))
                    .frequencia(FrequenciaRecorrencia.valueOf((String) r.get("frequencia")))
                    .diaReferencia(toInt(r.get("diaReferencia")))
                    .dataInicio(LocalDate.parse((String) r.get("dataInicio")))
                    .dataFim(r.get("dataFim") != null ? LocalDate.parse((String) r.get("dataFim")) : null)
                    .ativa(Boolean.TRUE.equals(r.get("ativa")))
                    .categoria(cat.get())
                    .usuario(usuario)
                    .build());
            recCount++;
        }

        return Map.of(
                "categorias", catIdMap.size(),
                "lancamentos", lancCount,
                "metas", metaCount,
                "orcamentos", orcCount,
                "cartoes", cartoes.size(),
                "recorrencias", recCount
        );
    }

    private BigDecimal toBD(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        return new BigDecimal(val.toString());
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Long) return (Long) val;
        return ((Number) val).longValue();
    }

    private Integer toInt(Object val) {
        if (val == null) return null;
        if (val instanceof Integer) return (Integer) val;
        return ((Number) val).intValue();
    }
}
