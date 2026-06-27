package com.awpy.awpy.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaRequest(
        @NotBlank(message = "e-mail é obrigatório")
        @Email(message = "e-mail em formato inválido")
        String email,

        @NotBlank(message = "código é obrigatório")
        String codigo,

        @NotBlank(message = "nova senha é obrigatória")
        @Size(min = 8, message = "nova senha deve ter no mínimo 8 caracteres")
        String novaSenha
) {
}
