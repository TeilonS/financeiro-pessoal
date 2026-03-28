package com.teilon.financeiro.dto;

import java.math.BigDecimal;

public record PeriodoResumo(
        int mes,
        int ano,
        BigDecimal totalReceitas,
        BigDecimal totalDespesas,
        BigDecimal saldo
) {}
