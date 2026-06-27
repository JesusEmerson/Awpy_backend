package com.awpy.awpy.dto.material;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MaterialCadastroRequest(
        @NotBlank(message = "nome é obrigatório")
        String nome,

        @NotNull(message = "pontos por Kg é obrigatório")
        @Positive(message = "pontos por Kg deve ser maior que zero")
        Double pontosPorKg
) {
}
