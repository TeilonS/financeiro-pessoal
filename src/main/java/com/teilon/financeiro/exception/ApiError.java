package com.teilon.financeiro.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
        int status,
        String mensagem,
        List<String> erros,
        LocalDateTime timestamp
) {
    public static ApiError of(int status, String mensagem) {
        return new ApiError(status, mensagem, List.of(), LocalDateTime.now());
    }

    public static ApiError of(int status, String mensagem, List<String> erros) {
        return new ApiError(status, mensagem, erros, LocalDateTime.now());
    }
}
