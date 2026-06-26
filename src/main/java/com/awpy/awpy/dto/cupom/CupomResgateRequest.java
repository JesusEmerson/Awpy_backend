package com.awpy.awpy.dto.cupom;

import jakarta.validation.constraints.NotNull;

public record CupomResgateRequest(
        @NotNull(message = "benefício é obrigatório")
        Long beneficioId
) {
}
