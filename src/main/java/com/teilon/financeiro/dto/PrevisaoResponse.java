package com.teilon.financeiro.dto;

import java.math.BigDecimal;

public record PrevisaoResponse(
        BigDecimal saldoAtual,
        BigDecimal receitasConfirmadas,
        BigDecimal despesasConfirmadas,
        BigDecimal receitasPendentes,
        BigDecimal despesasPendentes,
        BigDecimal saldoProjetado,
        int recorrenciasPendentes,
        int diasPassados,
        int totalDiasMes
) {}
