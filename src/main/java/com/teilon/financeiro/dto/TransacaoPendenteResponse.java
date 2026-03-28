package com.teilon.financeiro.dto;

import com.teilon.financeiro.model.TransacaoPendente;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoPendenteResponse(
        Long id,
        String descricao,
        BigDecimal valor,
        LocalDate data,
        String tipo,
        String status,
        Long categoriaSugeridaId,
        String categoriaSugeridaNome
) {
    public static TransacaoPendenteResponse of(TransacaoPendente t) {
        var cat = t.getCategoriaSugerida();
        return new TransacaoPendenteResponse(
                t.getId(),
                t.getDescricao(),
                t.getValor(),
                t.getData(),
                t.getTipo().name(),
                t.getStatus().name(),
                cat != null ? cat.getId() : null,
                cat != null ? cat.getNome() : null
        );
    }
}
