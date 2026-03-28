package com.teilon.financeiro.dto;

import java.util.List;

public record GerarLancamentosResponse(
        int gerados,
        List<LancamentoResponse> lancamentos
) {}
