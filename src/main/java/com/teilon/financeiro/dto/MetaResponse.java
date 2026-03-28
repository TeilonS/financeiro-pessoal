package com.teilon.financeiro.dto;

import com.teilon.financeiro.model.Meta;

import java.math.BigDecimal;

public record MetaResponse(
        Long id,
        Long categoriaId,
        String categoriaNome,
        BigDecimal valorLimite,
        int mes,
        int ano
) {
    public static MetaResponse de(Meta m) {
        return new MetaResponse(
                m.getId(),
                m.getCategoria().getId(),
                m.getCategoria().getNome(),
                m.getValorLimite(),
                m.getMes(),
                m.getAno()
        );
    }
}
