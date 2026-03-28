package com.teilon.financeiro.dto;

import com.teilon.financeiro.model.Recorrencia;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecorrenciaResponse(
        Long id,
        String descricao,
        BigDecimal valor,
        String tipo,
        String frequencia,
        Integer diaReferencia,
        LocalDate dataInicio,
        LocalDate dataFim,
        Boolean ativa,
        Long categoriaId,
        String categoriaNome
) {
    public static RecorrenciaResponse of(Recorrencia r) {
        return new RecorrenciaResponse(
                r.getId(),
                r.getDescricao(),
                r.getValor(),
                r.getTipo().name(),
                r.getFrequencia().name(),
                r.getDiaReferencia(),
                r.getDataInicio(),
                r.getDataFim(),
                r.getAtiva(),
                r.getCategoria().getId(),
                r.getCategoria().getNome()
        );
    }
}
