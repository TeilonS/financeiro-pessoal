package com.teilon.financeiro.dto;

import com.teilon.financeiro.model.Investimento;
import com.teilon.financeiro.model.TipoInvestimento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvestimentoResponse(
    Long id,
    String nome,
    String instituicao,
    TipoInvestimento tipo,
    BigDecimal saldoAtual,
    String ticker,
    BigDecimal cotas,
    BigDecimal precoUnitario,
    LocalDateTime ultimaAtualizacaoPreco
) {
    public static InvestimentoResponse of(Investimento i, BigDecimal saldoAtual) {
        return new InvestimentoResponse(
            i.getId(), i.getNome(), i.getInstituicao(), i.getTipo(), saldoAtual,
            i.getTicker(), i.getCotas(), i.getPrecoUnitario(), i.getUltimaAtualizacaoPreco()
        );
    }
}
