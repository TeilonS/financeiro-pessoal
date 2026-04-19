package com.teilon.financeiro.dto;

import com.teilon.financeiro.model.TipoInvestimento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InvestimentoRequest(
    @NotBlank String nome,
    @NotBlank String instituicao,
    @NotNull TipoInvestimento tipo
) {}
