package com.awpy.awpy.dto.usuario;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreditoPontosRequest(
        @NotNull(message = "pontos é obrigatório")
        @Positive(message = "pontos deve ser maior que zero")
        Long pontos
) {
}
