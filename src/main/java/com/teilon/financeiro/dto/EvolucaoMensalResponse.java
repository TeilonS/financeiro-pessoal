package com.teilon.financeiro.dto;

import java.math.BigDecimal;

public record EvolucaoMensalResponse(
        int mes,
        String nomeMes,
        BigDecimal totalReceitas,
        BigDecimal totalDespesas,
        BigDecimal saldo
) {}
