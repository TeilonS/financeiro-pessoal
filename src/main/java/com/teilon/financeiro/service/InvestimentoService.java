package com.teilon.financeiro.service;

import com.teilon.financeiro.dto.InvestimentoRequest;
import com.teilon.financeiro.dto.InvestimentoResponse;
import com.teilon.financeiro.dto.SnapshotRequest;
import com.teilon.financeiro.dto.SnapshotResponse;
import com.teilon.financeiro.model.Investimento;
import com.teilon.financeiro.model.SnapshotMensal;
import com.teilon.financeiro.model.Usuario;
import com.teilon.financeiro.repository.InvestimentoRepository;
import com.teilon.financeiro.repository.SnapshotMensalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvestimentoService {

    private final InvestimentoRepository investimentoRepository;
    private final SnapshotMensalRepository snapshotRepository;
    private final UsuarioService usuarioService;

    public List<InvestimentoResponse> listar() {
        Usuario usuario = usuarioService.getAutenticado();
        List<Investimento> investimentos = investimentoRepository.findAllByUsuario(usuario);
        
        List<SnapshotMensal> ultimos = snapshotRepository.findUltimosSnapshotsPorUsuario(usuario);
        Map<Long, BigDecimal> saldos = ultimos.stream()
                .collect(Collectors.toMap(s -> s.getInvestimento().getId(), SnapshotMensal::getValor));

        return investimentos.stream()
                .map(i -> InvestimentoResponse.of(i, saldos.getOrDefault(i.getId(), BigDecimal.ZERO)))
                .toList();
    }

    @Transactional
    public InvestimentoResponse criar(InvestimentoRequest request) {
        Usuario usuario = usuarioService.getAutenticado();
        Investimento i = Investimento.builder()
                .nome(request.nome())
                .instituicao(request.instituicao())
                .tipo(request.tipo())
                .usuario(usuario)
                .build();
        return InvestimentoResponse.of(investimentoRepository.save(i), BigDecimal.ZERO);
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
                        .investimento(i)
                        .mes(request.mes())
                        .ano(request.ano())
                        .usuario(usuario)
                        .build());

        s.setValor(request.valor());
        snapshotRepository.save(s);
    }

    public List<SnapshotResponse> listarHistorico(Long id) {
        Usuario usuario = usuarioService.getAutenticado();
        Investimento i = investimentoRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Investimento não encontrado"));

        return snapshotRepository.findAllByInvestimentoAndUsuarioOrderByAnoDescMesDesc(i, usuario)
                .stream()
                .map(SnapshotResponse::of)
                .toList();
    }

    public BigDecimal calcularTotalInvestido(Usuario usuario) {
        return snapshotRepository.findUltimosSnapshotsPorUsuario(usuario)
                .stream()
                .map(SnapshotMensal::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
