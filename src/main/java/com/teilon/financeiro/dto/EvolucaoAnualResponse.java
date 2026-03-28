package com.teilon.financeiro.dto;

import java.math.BigDecimal;
import java.util.List;

public record EvolucaoAnualResponse(
        int ano,
        List<EvolucaoMensalResponse> meses,
        BigDecimal totalReceitas,
        BigDecimal totalDespesas,
        BigDecimal saldo
) {}
