package com.awpy.awpy.dto.beneficio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BeneficioUpdateRequest(

        @NotBlank(message = "nome é obrigatório")
        String nome,

        @NotBlank(message = "descrição é obrigatória")
        String descricao,

        @NotNull(message = "custo em pontos é obrigatório")
        @Positive(message = "custo em pontos deve ser maior que zero")
        Long custoEmPontos,

        @NotNull(message = "ativo é obrigatório")
        Boolean ativo,

        Double percentualDesconto,

        Double percentualCashback
) {
}
