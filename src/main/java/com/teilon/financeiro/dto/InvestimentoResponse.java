package com.teilon.financeiro.dto;

import com.teilon.financeiro.model.Investimento;
import com.teilon.financeiro.model.TipoInvestimento;

import java.math.BigDecimal;

public record InvestimentoResponse(
    Long id,
    String nome,
    String instituicao,
    TipoInvestimento tipo,
    BigDecimal saldoAtual
) {
    public static InvestimentoResponse of(Investimento i, BigDecimal saldoAtual) {
        return new InvestimentoResponse(i.getId(), i.getNome(), i.getInstituicao(), i.getTipo(), saldoAtual);
    }
}
