package com.awpy.awpy.dto.material;

import com.awpy.awpy.model.Material;

public record MaterialResponse(
        Long id,
        String nome,
        Double pontosPorKg,
        Boolean ativo
) {
    public static MaterialResponse fromEntity(Material material) {
        return new MaterialResponse(material.getId(), material.getNome(), material.getPontosPorKg(), material.getAtivo());
    }
}
