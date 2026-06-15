package com.teilon.financeiro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetSenhaRequest(
    @NotBlank String token,
    @NotBlank @Size(min = 6) String novaSenha
) {}
