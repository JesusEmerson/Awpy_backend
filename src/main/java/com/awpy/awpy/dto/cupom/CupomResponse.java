package com.awpy.awpy.dto.cupom;

import com.awpy.awpy.model.Cupom;
import com.awpy.awpy.model.enums.StatusCupom;

import java.time.LocalDateTime;

public record CupomResponse(
        Long id,
        String beneficioNome,
        String parceiroNome,
        Long custoEmPontos,
        StatusCupom status,
        String qrCodeUnico,
        LocalDateTime dataGeracao,
        LocalDateTime dataExpiracao
) {
    public static CupomResponse fromEntity(Cupom cupom) {
        return new CupomResponse(
                cupom.getId(),
                cupom.getBeneficio().getNome(),
                cupom.getParceiro().getNomeEstabelecimento(),
                cupom.getBeneficio().getCustoEmPontos(),
                cupom.getStatus(),
                cupom.getQrCodeUnico(),
                cupom.getDataGeracao(),
                cupom.getDataExpiracao()
        );
    }
}
