package com.awpy.awpy.dto.beneficio;

import com.awpy.awpy.model.Beneficio;

public record BeneficioResponse(
        Long id,
        String nome,
        String descricao,
        Long custoEmPontos,
        Boolean ativo,
        Double percentualDesconto,
        Double percentualCashback,
        Long parceiroId,
        String parceiroNome
) {
    public static BeneficioResponse fromEntity(Beneficio beneficio) {
        return new BeneficioResponse(
                beneficio.getId(),
                beneficio.getNome(),
                beneficio.getDescricao(),
                beneficio.getCustoEmPontos(),
                beneficio.getAtivo(),
                beneficio.getPercentualDesconto(),
                beneficio.getPercentualCashback(),
                beneficio.getParceiro().getId(),
                beneficio.getParceiro().getNomeEstabelecimento()
        );
    }
}
