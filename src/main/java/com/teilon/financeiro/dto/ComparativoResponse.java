package com.teilon.financeiro.dto;

import java.math.BigDecimal;

public record ComparativoResponse(
        PeriodoResumo periodoAtual,
        PeriodoResumo periodoAnterior,
        BigDecimal variacaoReceitas,
        BigDecimal variacaoDespesas,
        BigDecimal variacaoSaldo
) {}
