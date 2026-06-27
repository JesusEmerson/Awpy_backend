package com.awpy.awpy.dto.reciclagem;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReciclagemRequest(
        @NotNull(message = "usuário é obrigatório")
        Long usuarioId,

        @NotNull(message = "material é obrigatório")
        Long materialId,

        @NotNull(message = "quilos é obrigatório")
        @Positive(message = "quilos deve ser maior que zero")
        Double quilos
) {
}
