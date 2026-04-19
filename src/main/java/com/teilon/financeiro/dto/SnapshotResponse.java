package com.teilon.financeiro.dto;

import com.teilon.financeiro.model.SnapshotMensal;
import java.math.BigDecimal;

public record SnapshotResponse(
    Long id,
    Integer mes,
    Integer ano,
    BigDecimal valor
) {
    public static SnapshotResponse of(SnapshotMensal s) {
        return new SnapshotResponse(s.getId(), s.getMes(), s.getAno(), s.getValor());
    }
}
