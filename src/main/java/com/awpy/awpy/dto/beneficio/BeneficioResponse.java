package com.awpy.awpy.dto.beneficio;

import com.awpy.awpy.model.Beneficio;

public record BeneficioResponse(
        Long id,
        String nome,
        String descricao,
        Long custoEmPontos,
        String parceiroNome
) {
    public static BeneficioResponse fromEntity(Beneficio beneficio) {
        return new BeneficioResponse(
                beneficio.getId(),
                beneficio.getNome(),
                beneficio.getDescricao(),
                beneficio.getCustoEmPontos(),
                beneficio.getParceiro().getNomeEstabelecimento()
        );
    }
}
