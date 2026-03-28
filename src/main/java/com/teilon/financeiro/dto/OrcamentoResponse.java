package com.teilon.financeiro.dto;

import java.math.BigDecimal;

public record OrcamentoResponse(
        Long id,
        Long categoriaId,
        String categoriaNome,
        String categoriaTipo,
        Integer mes,
        Integer ano,
        BigDecimal valorLimite,
        BigDecimal valorGasto,
        int percentualUsado
) {}
