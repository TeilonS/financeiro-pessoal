package com.teilon.financeiro.dto;

import com.teilon.financeiro.model.Provento;

import java.math.BigDecimal;

public record ProventoResponse(
    Long id,
    Long investimentoId,
    String investimentoNome,
    Integer mes,
    Integer ano,
    BigDecimal valor
) {
    public static ProventoResponse of(Provento p) {
        return new ProventoResponse(
            p.getId(), p.getInvestimento().getId(), p.getInvestimento().getNome(),
            p.getMes(), p.getAno(), p.getValor()
        );
    }
}
