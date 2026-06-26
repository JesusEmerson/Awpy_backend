package com.awpy.awpy.dto.parceiro;

import jakarta.validation.constraints.NotBlank;

public record ParceiroLoginRequest(
        @NotBlank(message = "e-mail é obrigatório")
        String email,

        @NotBlank(message = "senha é obrigatória")
        String senha
) {
}
