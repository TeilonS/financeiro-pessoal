package com.teilon.financeiro.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PreviewItemResponse(
        String descricao,
        BigDecimal valor,
        LocalDate data,
        String tipo,
        Long categoriaSugeridaId,
        String categoriaSugeridaNome,
        boolean ignorado
) {}
