package com.awpy.awpy.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record AdminFuncionarioLoginRequest(
        @NotBlank(message = "e-mail é obrigatório")
        String email,

        @NotBlank(message = "senha é obrigatória")
        String senha
) {
}
