package com.teilon.financeiro.service.parser;

import com.teilon.financeiro.model.TipoTransacao;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoImportada(
        String descricao,
        BigDecimal valor,
        LocalDate data,
        TipoTransacao tipo
) {}
