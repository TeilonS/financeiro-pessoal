package com.teilon.financeiro.dto;

import java.math.BigDecimal;

public record CartaoResponse(
        Long id,
        String nome,
        BigDecimal limite,
        BigDecimal faturaAtual,
        Integer diaVencimento,
        String cor,
        BigDecimal limiteDisponivel
) {}
