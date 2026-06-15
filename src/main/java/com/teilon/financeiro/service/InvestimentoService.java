package com.teilon.financeiro.service;

import com.teilon.financeiro.dto.InvestimentoRequest;
import com.teilon.financeiro.dto.InvestimentoResponse;
import com.teilon.financeiro.dto.SnapshotRequest;
import com.teilon.financeiro.dto.SnapshotResponse;
import com.teilon.financeiro.model.Investimento;
import com.teilon.financeiro.model.SnapshotMensal;
import com.teilon.financeiro.model.TipoInvestimento;
import com.teilon.financeiro.model.Usuario;
import com.teilon.financeiro.repository.InvestimentoRepository;
import com.teilon.financeiro.repository.SnapshotMensalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvestimentoService {

    private final InvestimentoRepository investimentoRepository;
    private final SnapshotMensalRepository snapshotRepository;
    private final UsuarioService usuarioService;
    private final CotacaoService cotacaoService;

    public List<InvestimentoResponse> listar() {
        Usuario usuario = usuarioService.getAutenticado();
        List<Investimento> investimentos = investimentoRepository.findAllByUsuario(usuario);

        Map<Long, BigDecimal> saldos = snapshotRepository.findUltimosSnapshotsPorUsuario(usuario).stream()
                .collect(Collectors.toMap(s -> s.getInvestimento().getId(), SnapshotMensal::getValor));

        return investimentos.stream()
                .map(i -> InvestimentoResponse.of(i, calcularSaldo(i, saldos)))
                .toList();
    }

    @Transactional
    public InvestimentoResponse criar(InvestimentoRequest request) {
        Usuario usuario = usuarioService.getAutenticado();

        String ticker = normalizarTicker(request.ticker(), request.tipo());
        BigDecimal preco = buscarPrecoSeAplicavel(ticker);

        Investimento i = Investimento.builder()
                .nome(request.nome())
                .instituicao(request.instituicao())
                .tipo(request.tipo())
                .ticker(ticker)
                .cotas(request.cotas())
                .precoUnitario(preco)
                .ultimaAtualizacaoPreco(preco != null ? LocalDateTime.now() : null)
                .usuario(usuario)
                .build();

        Investimento saved = investimentoRepository.save(i);
        return InvestimentoResponse.of(saved, calcularSaldo(saved, Map.of()));
    }

    @Transactional
    public InvestimentoResponse atualizar(Long id, InvestimentoRequest request) {
        Usuario usuario = usuarioService.getAutenticado();
        Investimento i = investimentoRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Investimento não encontrado"));

        String ticker = normalizarTicker(request.ticker(), request.tipo());

        // Se ticker mudou, busca novo preço
        boolean tickerMudou = ticker != null && !ticker.equals(i.getTicker());
        BigDecimal preco = tickerMudou ? buscarPrecoSeAplicavel(ticker) : i.getPrecoUnitario();
        LocalDateTime ultimaAtu = tickerMudou && preco != null ? LocalDateTime.now() : i.getUltimaAtualizacaoPreco();

        i.setNome(request.nome());
        i.setInstituicao(request.instituicao());
        i.setTipo(request.tipo());
        i.setTicker(ticker);
        i.setCotas(request.cotas());
        i.setPrecoUnitario(preco);
        i.setUltimaAtualizacaoPreco(ultimaAtu);

        Investimento saved = investimentoRepository.save(i);

        Map<Long, BigDecimal> saldos = snapshotRepository.findUltimosSnapshotsPorUsuario(usuario).stream()
                .collect(Collectors.toMap(s -> s.getInvestimento().getId(), SnapshotMensal::getValor));
        return InvestimentoResponse.of(saved, calcularSaldo(saved, saldos));
    }

    @Transactional
    public void deletar(Long id) {
        Usuario usuario = usuarioService.getAutenticado();
        Investimento i = investimentoRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Investimento não encontrado"));
        investimentoRepository.delete(i);
    }

    @Transactional
    public void registrarSnapshot(Long id, SnapshotRequest request) {
        Usuario usuario = usuarioService.getAutenticado();
        Investimento i = investimentoRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Investimento não encontrado"));

        SnapshotMensal s = snapshotRepository.findByInvestimentoAndMesAndAnoAndUsuario(i, request.mes(), request.ano(), usuario)
                .orElse(SnapshotMensal.builder()
                        .investimento(i).mes(request.mes()).ano(request.ano()).usuario(usuario).build());
        s.setValor(request.valor());
        snapshotRepository.save(s);
    }

    public List<SnapshotResponse> listarHistorico(Long id) {
        Usuario usuario = usuarioService.getAutenticado();
        Investimento i = investimentoRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Investimento não encontrado"));
        return snapshotRepository.findAllByInvestimentoAndUsuarioOrderByAnoDescMesDesc(i, usuario)
                .stream().map(SnapshotResponse::of).toList();
    }

    /** Atualiza o preço de todos os ativos com ticker cadastrado. */
    @Transactional
    public int atualizarCotacoes() {
        Usuario usuario = usuarioService.getAutenticado();
        List<Investimento> comTicker = investimentoRepository.findAllByUsuario(usuario).stream()
                .filter(i -> i.getTicker() != null && !i.getTicker().isBlank())
                .toList();

        int atualizados = 0;
        for (Investimento i : comTicker) {
            BigDecimal preco = cotacaoService.buscarPreco(i.getTicker());
            if (preco != null) {
                i.setPrecoUnitario(preco);
                i.setUltimaAtualizacaoPreco(LocalDateTime.now());
                investimentoRepository.save(i);
                atualizados++;
            }
        }
        return atualizados;
    }

    public BigDecimal calcularTotalInvestido(Usuario usuario) {
        List<Investimento> todos = investimentoRepository.findAllByUsuario(usuario);
        Map<Long, BigDecimal> saldos = snapshotRepository.findUltimosSnapshotsPorUsuario(usuario).stream()
                .collect(Collectors.toMap(s -> s.getInvestimento().getId(), SnapshotMensal::getValor));
        return todos.stream()
                .map(i -> calcularSaldo(i, saldos))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ─── helpers ───────────────────────────────────────────────────────────

    private BigDecimal calcularSaldo(Investimento i, Map<Long, BigDecimal> saldosPorSnapshot) {
        if (i.getTicker() != null && i.getCotas() != null && i.getPrecoUnitario() != null
                && i.getPrecoUnitario().compareTo(BigDecimal.ZERO) > 0) {
            return i.getCotas().multiply(i.getPrecoUnitario());
        }
        return saldosPorSnapshot.getOrDefault(i.getId(), BigDecimal.ZERO);
    }

    private String normalizarTicker(String ticker, TipoInvestimento tipo) {
        if (ticker == null || ticker.isBlank()) return null;
        if (tipo != TipoInvestimento.ACOES && tipo != TipoInvestimento.FII) return null;
        return ticker.toUpperCase().trim();
    }

    private BigDecimal buscarPrecoSeAplicavel(String ticker) {
        if (ticker == null) return null;
        BigDecimal preco = cotacaoService.buscarPreco(ticker);
        if (preco == null) log.warn("Não foi possível buscar preço para ticker={}", ticker);
        return preco;
    }
}
