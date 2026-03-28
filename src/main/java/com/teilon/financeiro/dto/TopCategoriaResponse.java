package com.teilon.financeiro.dto;

import java.math.BigDecimal;

public record TopCategoriaResponse(
        Long categoriaId,
        String categoriaNome,
        String tipo,
        BigDecimal total,
        int percentual
) {}
