package com.teilon.financeiro.dto;

import com.teilon.financeiro.model.FrequenciaRecorrencia;
import com.teilon.financeiro.model.TipoTransacao;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecorrenciaRequest(
        @NotBlank String descricao,
        @NotNull @Positive BigDecimal valor,
        @NotNull TipoTransacao tipo,
        @NotNull FrequenciaRecorrencia frequencia,
        @NotNull @Min(1) @Max(28) Integer diaReferencia,
        @NotNull LocalDate dataInicio,
        LocalDate dataFim,
        @NotNull Long categoriaId
) {}
