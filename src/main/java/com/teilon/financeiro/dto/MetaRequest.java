package com.teilon.financeiro.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record MetaRequest(
        @NotNull(message = "Categoria é obrigatória")
        Long categoriaId,

        @NotNull(message = "Valor limite é obrigatório")
        @Positive(message = "Valor limite deve ser positivo")
        BigDecimal valorLimite,

        @NotNull(message = "Mês é obrigatório")
        @Min(value = 1, message = "Mês deve ser entre 1 e 12")
        @Max(value = 12, message = "Mês deve ser entre 1 e 12")
        Integer mes,

        @NotNull(message = "Ano é obrigatório")
        @Min(value = 2000, message = "Ano inválido")
        Integer ano
) {}
