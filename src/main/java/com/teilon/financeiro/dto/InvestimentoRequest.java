package com.teilon.financeiro.dto;

import com.teilon.financeiro.model.TipoInvestimento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record InvestimentoRequest(
    @NotBlank String nome,
    @NotBlank String instituicao,
    @NotNull TipoInvestimento tipo,
    String ticker,
    BigDecimal cotas
) {}
