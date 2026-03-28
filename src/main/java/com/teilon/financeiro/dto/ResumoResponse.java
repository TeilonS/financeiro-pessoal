package com.teilon.financeiro.dto;

import java.math.BigDecimal;
import java.util.List;

public record ResumoResponse(
        int mes,
        int ano,
        BigDecimal totalReceitas,
        BigDecimal totalDespesas,
        BigDecimal saldo,
        List<CategoriaSaldo> breakdown
) {
    public record CategoriaSaldo(
            Long categoriaId,
            String categoriaNome,
            String tipo,
            BigDecimal total
    ) {}
}
