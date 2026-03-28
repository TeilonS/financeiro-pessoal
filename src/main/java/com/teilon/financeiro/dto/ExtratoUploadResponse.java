package com.teilon.financeiro.dto;

import java.util.List;

public record ExtratoUploadResponse(
        Long extratoId,
        String nomeArquivo,
        String formato,
        int totalTransacoes,
        int autoConfirmadas,
        List<TransacaoPendenteResponse> transacoes
) {}
