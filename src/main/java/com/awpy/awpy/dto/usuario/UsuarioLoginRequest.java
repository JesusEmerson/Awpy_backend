package com.awpy.awpy.dto.usuario;

import jakarta.validation.constraints.NotBlank;

public record UsuarioLoginRequest(
        @NotBlank(message = "e-mail é obrigatório")
        String email,

        @NotBlank(message = "senha é obrigatória")
        String senha
) {
}
