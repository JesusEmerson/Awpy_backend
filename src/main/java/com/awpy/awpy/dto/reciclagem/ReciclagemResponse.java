package com.awpy.awpy.dto.reciclagem;

public record ReciclagemResponse(
        Long pontosCreditados,
        Long novoSaldoPontos,
        Double novoTotalKg
) {
}
