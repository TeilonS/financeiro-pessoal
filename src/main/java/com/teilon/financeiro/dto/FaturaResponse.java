package com.teilon.financeiro.dto;

import java.math.BigDecimal;

public record FaturaResponse(Long id, Integer mes, Integer ano, BigDecimal valor) {}
