package com.teilon.financeiro.dto;

import jakarta.validation.constraints.NotNull;

public record ConfirmarTransacaoRequest(
        @NotNull Long categoriaId
) {}
