package com.awpy.awpy.dto.parceiro;

import com.awpy.awpy.model.Parceiro;

public record ParceiroResponse(
        Long id,
        String nomeEstabelecimento,
        String email,
        String fotoUrl
) {
    public static ParceiroResponse fromEntity(Parceiro parceiro) {
        return new ParceiroResponse(
                parceiro.getId(), parceiro.getNomeEstabelecimento(), parceiro.getEmail(), parceiro.getFotoUrl());
    }
}
