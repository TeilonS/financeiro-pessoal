package com.teilon.financeiro.dto;

import com.teilon.financeiro.model.Lancamento;
import com.teilon.financeiro.model.TipoTransacao;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LancamentoResponse(
        Long id,
        String descricao,
        BigDecimal valor,
        LocalDate data,
        TipoTransacao tipo,
        Long categoriaId,
        String categoriaNome
) {
    public static LancamentoResponse de(Lancamento l) {
        return new LancamentoResponse(
                l.getId(),
                l.getDescricao(),
                l.getValor(),
                l.getData(),
                l.getTipo(),
                l.getCategoria().getId(),
                l.getCategoria().getNome()
        );
    }
}
