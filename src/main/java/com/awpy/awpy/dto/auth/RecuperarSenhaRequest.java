package com.awpy.awpy.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RecuperarSenhaRequest(
        @NotBlank(message = "e-mail é obrigatório")
        @Email(message = "e-mail em formato inválido")
        String email
) {
}
