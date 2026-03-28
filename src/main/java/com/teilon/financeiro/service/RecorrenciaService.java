package com.teilon.financeiro.service;

import com.teilon.financeiro.dto.*;
import com.teilon.financeiro.model.*;
import com.teilon.financeiro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecorrenciaService {

    private final RecorrenciaRepository recorrenciaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioService usuarioService;

    @Transactional
    public RecorrenciaResponse criar(RecorrenciaRequest req) {
        Usuario usuario = usuarioService.getAutenticado();

        Categoria categoria = categoriaRepository.findByIdAndUsuario(req.categoriaId(), usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));

        Recorrencia recorrencia = recorrenciaRepository.save(Recorrencia.builder()
                .descricao(req.descricao())
                .valor(req.valor())
                .tipo(req.tipo())
                .frequencia(req.frequencia())
                .diaReferencia(req.diaReferencia())
                .dataInicio(req.dataInicio())
                .dataFim(req.dataFim())
                .ativa(true)
                .categoria(categoria)
                .usuario(usuario)
                .build());

        return RecorrenciaResponse.of(recorrencia);
    }

    public List<RecorrenciaResponse> listar() {
        Usuario usuario = usuarioService.getAutenticado();
        return recorrenciaRepository.findAllByUsuario(usuario)
                .stream().map(RecorrenciaResponse::of).toList();
    }

    @Transactional
    public void desativar(Long id) {
        Usuario usuario = usuarioService.getAutenticado();
        Recorrencia r = recorrenciaRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recorrência não encontrada"));
        r.setAtiva(false);
        recorrenciaRepository.save(r);
    }

    /**
     * Gera os lançamentos de todas as recorrências ativas para o mês/ano informado.
     * Ignora recorrências cujo diaReferencia cai fora do intervalo do mês
     * ou que estão fora do período dataInicio–dataFim.
     * Não duplica: verifica se já existe lançamento com mesma descrição, valor e data.
     */
    @Transactional
    public GerarLancamentosResponse gerarParaMes(int mes, int ano) {
        Usuario usuario = usuarioService.getAutenticado();
        List<Recorrencia> ativas = recorrenciaRepository.findAllByUsuarioAndAtivaTrue(usuario);

        List<LancamentoResponse> gerados = new ArrayList<>();

        for (Recorrencia r : ativas) {
            List<LocalDate> datas = calcularDatasNoMes(r, mes, ano);

            for (LocalDate data : datas) {
                if (jaExiste(usuario, r, data)) continue;

                Lancamento l = lancamentoRepository.save(Lancamento.builder()
                        .descricao(r.getDescricao())
                        .valor(r.getValor())
                        .data(data)
                        .tipo(r.getTipo())
                        .categoria(r.getCategoria())
                        .usuario(usuario)
                        .build());

                gerados.add(LancamentoResponse.de(l));
            }
        }

        return new GerarLancamentosResponse(gerados.size(), gerados);
    }

    private List<LocalDate> calcularDatasNoMes(Recorrencia r, int mes, int ano) {
        List<LocalDate> datas = new ArrayList<>();
        LocalDate inicioMes = LocalDate.of(ano, mes, 1);
        LocalDate fimMes = inicioMes.with(TemporalAdjusters.lastDayOfMonth());

        switch (r.getFrequencia()) {
            case MENSAL -> {
                int dia = Math.min(r.getDiaReferencia(), fimMes.getDayOfMonth());
                LocalDate data = LocalDate.of(ano, mes, dia);
                if (dentroDoPeriodo(r, data)) datas.add(data);
            }
            case ANUAL -> {
                // diaReferencia = mês do ano (1–12); só gera se o mês bater
                if (r.getDiaReferencia() == mes) {
                    LocalDate data = LocalDate.of(ano, mes, 1);
                    if (dentroDoPeriodo(r, data)) datas.add(data);
                }
            }
            case SEMANAL -> {
                // diaReferencia = dia da semana (1=seg..7=dom)
                DayOfWeek dow = DayOfWeek.of(r.getDiaReferencia());
                LocalDate data = inicioMes.with(TemporalAdjusters.nextOrSame(dow));
                while (!data.isAfter(fimMes)) {
                    if (dentroDoPeriodo(r, data)) datas.add(data);
                    data = data.plusWeeks(1);
                }
            }
        }
        return datas;
    }

    private boolean dentroDoPeriodo(Recorrencia r, LocalDate data) {
        if (data.isBefore(r.getDataInicio())) return false;
        return r.getDataFim() == null || !data.isAfter(r.getDataFim());
    }

    private boolean jaExiste(Usuario usuario, Recorrencia r, LocalDate data) {
        return lancamentoRepository
                .findAllByUsuarioAndMesAndAno(usuario, data.getMonthValue(), data.getYear())
                .stream()
                .anyMatch(l -> l.getDescricao().equals(r.getDescricao())
                        && l.getValor().compareTo(r.getValor()) == 0
                        && l.getData().equals(data));
    }
}
