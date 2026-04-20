package com.teilon.financeiro.service;

import com.teilon.financeiro.dto.ConfirmarTransacaoRequest;
import com.teilon.financeiro.dto.ExtratoUploadResponse;
import com.teilon.financeiro.dto.PreviewItemResponse;
import com.teilon.financeiro.dto.TransacaoPendenteResponse;
import com.teilon.financeiro.model.*;
import com.teilon.financeiro.repository.*;
import com.teilon.financeiro.service.parser.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExtratoService {

    private static final Logger log = LoggerFactory.getLogger(ExtratoService.class);

    private final OfxParser ofxParser;
    private final CsvInterParser csvInterParser;
    private final CsvC6Parser csvC6Parser;
    private final CsvNubankParser csvNubankParser;
    private final CsvItauParser csvItauParser;
    private final CategorizadorAutomatico categorizador;

    private final ExtratoBrutoRepository extratoRepository;
    private final TransacaoPendenteRepository transacaoPendenteRepository;
    private final CategoriaRepository categoriaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final RegraCategorizacaoRepository regraRepository;
    private final UsuarioService usuarioService;

    @Transactional
    public ExtratoUploadResponse importar(MultipartFile file, FormatoExtrato formato) {
        Usuario usuario = usuarioService.getAutenticado();

        List<TransacaoImportada> importadas;
        try {
            importadas = switch (formato) {
                case OFX -> ofxParser.parse(file);
                case CSV_INTER -> csvInterParser.parse(file);
                case CSV_C6 -> csvC6Parser.parse(file);
                case CSV_NUBANK -> csvNubankParser.parse(file);
                case CSV_ITAU -> csvItauParser.parse(file);
            };
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Erro ao processar arquivo: " + e.getMessage());
        }

        if (importadas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Nenhuma transação encontrada no arquivo. Verifique o formato selecionado.");
        }

        ExtratoBruto extrato = extratoRepository.save(ExtratoBruto.builder()
                .nomeArquivo(file.getOriginalFilename())
                .formato(formato)
                .dataImportacao(LocalDateTime.now())
                .usuario(usuario)
                .build());

        List<Categoria> todasCategorias = categoriaRepository.findAllByUsuario(usuario);
        List<RegraCategorizacao> regrasUsuario = regraRepository.findAllByUsuario(usuario);

        List<TransacaoPendente> todasPendentes = new ArrayList<>();
        List<Lancamento> autoLancamentos = new ArrayList<>();

        for (TransacaoImportada t : importadas) {
            // Transferência entre contas próprias → ignora completamente
            if (categorizador.deveIgnorar(t.descricao())) continue;

            Categoria sugerida = categorizador
                    .sugerir(t.descricao(), t.tipo(), todasCategorias, regrasUsuario)
                    .orElse(null);

            TransacaoPendente pendente = TransacaoPendente.builder()
                    .descricao(t.descricao())
                    .valor(t.valor())
                    .data(t.data())
                    .tipo(t.tipo())
                    .extrato(extrato)
                    .usuario(usuario)
                    .categoriaSugerida(sugerida)
                    .status(sugerida != null ? StatusTransacao.CONFIRMADA : StatusTransacao.PENDENTE)
                    .build();

            if (sugerida != null) {
                Lancamento lancamento = Lancamento.builder()
                        .descricao(t.descricao())
                        .valor(t.valor())
                        .data(t.data())
                        .tipo(t.tipo())
                        .categoria(sugerida)
                        .usuario(usuario)
                        .build();
                autoLancamentos.add(lancamento);
                // associa após salvar (feito abaixo)
                todasPendentes.add(pendente);
            } else {
                todasPendentes.add(pendente);
            }
        }

        // Salva lançamentos automáticos e associa às pendentes
        if (!autoLancamentos.isEmpty()) {
            lancamentoRepository.saveAll(autoLancamentos);
            int idx = 0;
            for (TransacaoPendente p : todasPendentes) {
                if (p.getStatus() == StatusTransacao.CONFIRMADA) {
                    p.setLancamento(autoLancamentos.get(idx++));
                }
            }
        }

        transacaoPendenteRepository.saveAll(todasPendentes);

        // Retorna somente as que ficaram pendentes para o usuário resolver
        List<TransacaoPendenteResponse> responses = todasPendentes.stream()
                .filter(p -> p.getStatus() == StatusTransacao.PENDENTE)
                .map(TransacaoPendenteResponse::of)
                .toList();

        return new ExtratoUploadResponse(
                extrato.getId(),
                extrato.getNomeArquivo(),
                extrato.getFormato().name(),
                importadas.size(),
                autoLancamentos.size(),
                responses
        );
    }

    public List<TransacaoPendenteResponse> listarPendentes() {
        Usuario usuario = usuarioService.getAutenticado();
        return transacaoPendenteRepository
                .findAllByUsuarioAndStatus(usuario, StatusTransacao.PENDENTE)
                .stream()
                .map(TransacaoPendenteResponse::of)
                .toList();
    }

    @Transactional
    public void confirmar(Long id, ConfirmarTransacaoRequest request) {
        Usuario usuario = usuarioService.getAutenticado();

        TransacaoPendente transacao = transacaoPendenteRepository
                .findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transação não encontrada"));

        if (transacao.getStatus() != StatusTransacao.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Transação já foi " + transacao.getStatus().name().toLowerCase());
        }

        Categoria categoria = categoriaRepository
                .findByIdAndUsuario(request.categoriaId(), usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));

        Lancamento lancamento = lancamentoRepository.save(Lancamento.builder()
                .descricao(transacao.getDescricao())
                .valor(transacao.getValor())
                .data(transacao.getData())
                .tipo(transacao.getTipo())
                .categoria(categoria)
                .usuario(usuario)
                .build());

        transacao.setStatus(StatusTransacao.CONFIRMADA);
        transacao.setLancamento(lancamento);
        transacaoPendenteRepository.save(transacao);

        // Aprende: salva/atualiza regra para essa descrição
        String chave = normalizarChave(transacao.getDescricao());
        if (!chave.isBlank()) {
            regraRepository.findByChaveAndUsuario(chave, usuario).ifPresentOrElse(
                regra -> { regra.setCategoria(categoria); regraRepository.save(regra); },
                () -> regraRepository.save(RegraCategorizacao.builder()
                        .chave(chave).categoria(categoria).usuario(usuario).build())
            );

            // Aplica a nova regra em todas as pendentes restantes
            List<TransacaoPendente> outrasPendentes = transacaoPendenteRepository
                    .findAllByUsuarioAndStatus(usuario, StatusTransacao.PENDENTE);

            List<Lancamento> novosLancamentos = new ArrayList<>();
            for (TransacaoPendente p : outrasPendentes) {
                if (p.getId().equals(id)) continue;
                if (!normalizarChave(p.getDescricao()).contains(chave)
                        && !chave.contains(normalizarChave(p.getDescricao()))) continue;

                Lancamento l = Lancamento.builder()
                        .descricao(p.getDescricao())
                        .valor(p.getValor())
                        .data(p.getData())
                        .tipo(p.getTipo())
                        .categoria(categoria)
                        .usuario(usuario)
                        .build();
                novosLancamentos.add(l);
                p.setCategoriaSugerida(categoria);
                p.setStatus(StatusTransacao.CONFIRMADA);
            }

            if (!novosLancamentos.isEmpty()) {
                lancamentoRepository.saveAll(novosLancamentos);
                int idx = 0;
                for (TransacaoPendente p : outrasPendentes) {
                    if (p.getStatus() == StatusTransacao.CONFIRMADA && p.getLancamento() == null) {
                        p.setLancamento(novosLancamentos.get(idx++));
                    }
                }
                transacaoPendenteRepository.saveAll(outrasPendentes);
            }
        }
    }

    @Transactional
    public void confirmarLote(List<Map<String, Long>> itens) {
        List<Long> falhas = new ArrayList<>();
        for (Map<String, Long> item : itens) {
            try {
                confirmar(item.get("id"), new ConfirmarTransacaoRequest(item.get("categoriaId")));
            } catch (Exception ex) {
                Long id = item.get("id");
                falhas.add(id);
                log.warn("Falha ao confirmar transação id={}: {}", id, ex.getMessage());
            }
        }
        if (!falhas.isEmpty()) {
            throw new IllegalStateException("Falha ao confirmar " + falhas.size() + " transação(ões): IDs " + falhas);
        }
    }

    /** Normaliza a descrição para usar como chave de aprendizado. */
    private String normalizarChave(String descricao) {
        if (descricao == null) return "";
        return descricao.toLowerCase()
                .replace("ã", "a").replace("â", "a").replace("á", "a").replace("à", "a")
                .replace("ç", "c").replace("é", "e").replace("ê", "e")
                .replace("í", "i").replace("ó", "o").replace("ô", "o").replace("ú", "u")
                .replaceAll("\\s+", " ").trim();
    }

    @Transactional
    public void ignorar(Long id) {
        Usuario usuario = usuarioService.getAutenticado();

        TransacaoPendente transacao = transacaoPendenteRepository
                .findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transação não encontrada"));

        if (transacao.getStatus() != StatusTransacao.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Transação já foi " + transacao.getStatus().name().toLowerCase());
        }

        transacao.setStatus(StatusTransacao.IGNORADA);
        transacaoPendenteRepository.save(transacao);
    }

    public List<PreviewItemResponse> preview(MultipartFile file, FormatoExtrato formato) {
        Usuario usuario = usuarioService.getAutenticado();
        List<TransacaoImportada> importadas;
        try {
            importadas = switch (formato) {
                case OFX -> ofxParser.parse(file);
                case CSV_INTER -> csvInterParser.parse(file);
                case CSV_C6 -> csvC6Parser.parse(file);
                case CSV_NUBANK -> csvNubankParser.parse(file);
                case CSV_ITAU -> csvItauParser.parse(file);
            };
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro ao processar arquivo: " + e.getMessage());
        }
        List<Categoria> cats = categoriaRepository.findAllByUsuario(usuario);
        List<RegraCategorizacao> regras = regraRepository.findAllByUsuario(usuario);
        return importadas.stream().map(t -> {
            boolean ignorado = categorizador.deveIgnorar(t.descricao());
            Categoria sugerida = ignorado ? null :
                    categorizador.sugerir(t.descricao(), t.tipo(), cats, regras).orElse(null);
            return new PreviewItemResponse(
                    t.descricao(), t.valor(), t.data(), t.tipo().name(),
                    sugerida != null ? sugerida.getId() : null,
                    sugerida != null ? sugerida.getNome() : null,
                    ignorado
            );
        }).toList();
    }
}
