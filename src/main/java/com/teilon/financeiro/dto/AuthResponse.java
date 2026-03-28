package com.teilon.financeiro.dto;

public record AuthResponse(
        String token,
        String email,
        String nome
) {}
