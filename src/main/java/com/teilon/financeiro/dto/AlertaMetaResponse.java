package com.teilon.financeiro.dto;

import java.math.BigDecimal;

public record AlertaMetaResponse(
        Long metaId,
        Long categoriaId,
        String categoriaNome,
        int mes,
        int ano,
        BigDecimal valorLimite,
        BigDecimal totalGasto,
        BigDecimal excedente
) {}
