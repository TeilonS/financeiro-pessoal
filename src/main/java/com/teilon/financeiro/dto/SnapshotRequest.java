package com.teilon.financeiro.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SnapshotRequest(
    @NotNull @Min(1) @Max(12) Integer mes,
    @NotNull @Min(2000) Integer ano,
    @NotNull BigDecimal valor
) {}
