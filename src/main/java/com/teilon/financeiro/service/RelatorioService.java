package com.teilon.financeiro.service;

import com.teilon.financeiro.dto.*;
import com.teilon.financeiro.model.Categoria;
import com.teilon.financeiro.model.Lancamento;
import com.teilon.financeiro.model.Recorrencia;
import com.teilon.financeiro.model.TipoTransacao;
import com.teilon.financeiro.model.Usuario;
import com.teilon.financeiro.repository.LancamentoRepository;
import com.teilon.financeiro.repository.RecorrenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final LancamentoRepository lancamentoRepository;
    private final RecorrenciaRepository recorrenciaRepository;
    private final UsuarioService usuarioService;

    private static final Locale PT_BR = new Locale("pt", "BR");

    public EvolucaoAnualResponse evolucaoAnual(int ano) {
        Usuario usuario = usuarioService.getAutenticado();

        Map<Integer, BigDecimal> receitas = mapearPorMes(
                lancamentoRepository.sumPorMesNoAno(usuario, ano, TipoTransacao.RECEITA));
        Map<Integer, BigDecimal> despesas = mapearPorMes(
                lancamentoRepository.sumPorMesNoAno(usuario, ano, TipoTransacao.DESPESA));

        List<EvolucaoMensalResponse> meses = new ArrayList<>();
        BigDecimal totalReceitas = BigDecimal.ZERO;
        BigDecimal totalDespesas = BigDecimal.ZERO;

        for (int mes = 1; mes <= 12; mes++) {
            BigDecimal r = receitas.getOrDefault(mes, BigDecimal.ZERO);
            BigDecimal d = despesas.getOrDefault(mes, BigDecimal.ZERO);
            String nomeMes = Month.of(mes).getDisplayName(TextStyle.FULL, PT_BR);

            meses.add(new EvolucaoMensalResponse(mes, nomeMes, r, d, r.subtract(d)));
            totalReceitas = totalReceitas.add(r);
            totalDespesas = totalDespesas.add(d);
        }

        return new EvolucaoAnualResponse(
                ano, meses, totalReceitas, totalDespesas,
                totalReceitas.subtract(totalDespesas));
    }

    public List<TopCategoriaResponse> topCategorias(int mes, int ano, TipoTransacao tipo) {
        Usuario usuario = usuarioService.getAutenticado();

        List<Object[]> rows = lancamentoRepository.topCategoriasPorMes(usuario, mes, ano, tipo);
        if (rows.isEmpty()) return List.of();

        BigDecimal somaTudo = rows.stream()
                .map(r -> (BigDecimal) r[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return rows.stream().map(row -> {
            Categoria cat = (Categoria) row[0];
            BigDecimal total = (BigDecimal) row[1];
            int percentual = somaTudo.compareTo(BigDecimal.ZERO) == 0 ? 0 :
                    total.multiply(BigDecimal.valueOf(100))
                            .divide(somaTudo, 0, RoundingMode.HALF_UP)
                            .intValue();
            return new TopCategoriaResponse(
                    cat.getId(), cat.getNome(), cat.getTipo().name(), total, percentual);
        }).collect(Collectors.toList());
    }

    public ComparativoResponse comparativo(int mesAtual, int anoAtual, int mesAnterior, int anoAnterior) {
        Usuario usuario = usuarioService.getAutenticado();

        PeriodoResumo atual = calcularResumo(usuario, mesAtual, anoAtual);
        PeriodoResumo anterior = calcularResumo(usuario, mesAnterior, anoAnterior);

        BigDecimal varReceitas = calcularVariacao(anterior.totalReceitas(), atual.totalReceitas());
        BigDecimal varDespesas = calcularVariacao(anterior.totalDespesas(), atual.totalDespesas());
        BigDecimal varSaldo = calcularVariacao(anterior.saldo(), atual.saldo());

        return new ComparativoResponse(atual, anterior, varReceitas, varDespesas, varSaldo);
    }

    private PeriodoResumo calcularResumo(Usuario usuario, int mes, int ano) {
        BigDecimal receitas = lancamentoRepository.sumPorTipoEPeriodo(usuario, mes, ano, TipoTransacao.RECEITA);
        BigDecimal despesas = lancamentoRepository.sumPorTipoEPeriodo(usuario, mes, ano, TipoTransacao.DESPESA);
        return new PeriodoResumo(mes, ano, receitas, despesas, receitas.subtract(despesas));
    }

    /**
     * Retorna a variação percentual entre base e atual.
     * Null quando a base é zero (não há como calcular percentual).
     */
    private BigDecimal calcularVariacao(BigDecimal base, BigDecimal atual) {
        if (base.compareTo(BigDecimal.ZERO) == 0) return null;
        return atual.subtract(base)
                .multiply(BigDecimal.valueOf(100))
                .divide(base.abs(), 2, RoundingMode.HALF_UP);
    }

    public PrevisaoResponse previsao(int mes, int ano) {
        Usuario usuario = usuarioService.getAutenticado();

        BigDecimal recConf  = lancamentoRepository.sumPorTipoEPeriodo(usuario, mes, ano, TipoTransacao.RECEITA);
        BigDecimal despConf = lancamentoRepository.sumPorTipoEPeriodo(usuario, mes, ano, TipoTransacao.DESPESA);
        BigDecimal saldoAtual = recConf.subtract(despConf);

        LocalDate hoje = LocalDate.now();
        int totalDias  = YearMonth.of(ano, mes).lengthOfMonth();
        int diasPassados = (hoje.getYear() == ano && hoje.getMonthValue() == mes)
                ? hoje.getDayOfMonth()
                : totalDias;

        // Recorrências pendentes: ativas que ainda não geraram lançamento neste mês
        List<Lancamento> lancamentosMes = lancamentoRepository.findAllByUsuarioAndMesAndAno(usuario, mes, ano);
        List<Recorrencia> ativas = recorrenciaRepository.findAllByUsuarioAndAtivaTrue(usuario);

        BigDecimal recPendentes  = BigDecimal.ZERO;
        BigDecimal despPendentes = BigDecimal.ZERO;
        int totalPendentes = 0;

        for (Recorrencia r : ativas) {
            for (LocalDate data : calcularDatasNoMes(r, mes, ano)) {
                boolean jaExiste = lancamentosMes.stream().anyMatch(l ->
                        l.getDescricao().equals(r.getDescricao())
                        && l.getValor().compareTo(r.getValor()) == 0
                        && l.getData().equals(data));

                if (!jaExiste) {
                    if (r.getTipo() == TipoTransacao.RECEITA) {
                        recPendentes = recPendentes.add(r.getValor());
                    } else {
                        despPendentes = despPendentes.add(r.getValor());
                    }
                    totalPendentes++;
                }
            }
        }

        BigDecimal saldoProjetado = saldoAtual.add(recPendentes).subtract(despPendentes);

        return new PrevisaoResponse(saldoAtual, recConf, despConf, recPendentes, despPendentes,
                saldoProjetado, totalPendentes, diasPassados, totalDias);
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
                if (r.getDiaReferencia() == mes) {
                    LocalDate data = LocalDate.of(ano, mes, 1);
                    if (dentroDoPeriodo(r, data)) datas.add(data);
                }
            }
            case SEMANAL -> {
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

    private Map<Integer, BigDecimal> mapearPorMes(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
                r -> ((Number) r[0]).intValue(),
                r -> (BigDecimal) r[1]
        ));
    }
}
