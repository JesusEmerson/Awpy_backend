package com.awpy.awpy.dto.cupom;

import com.awpy.awpy.model.Cupom;
import com.awpy.awpy.model.enums.StatusCupom;

import java.time.LocalDateTime;

public record CardValidacaoResponse(
        Long cupomId,
        String qrCodeUnico,
        StatusCupom status,
        LocalDateTime dataExpiracao,
        String beneficioNome,
        String parceiroNome,
        Long pontos,
        DadosUsuario usuario
) {
    public record DadosUsuario(Long id, String nome, String fotoUrl) {
    }

    public static CardValidacaoResponse fromEntity(Cupom cupom) {
        return new CardValidacaoResponse(
                cupom.getId(),
                cupom.getQrCodeUnico(),
                cupom.getStatus(),
                cupom.getDataExpiracao(),
                cupom.getBeneficio().getNome(),
                cupom.getParceiro().getNomeEstabelecimento(),
                cupom.getBeneficio().getCustoEmPontos(),
                new DadosUsuario(
                        cupom.getUsuario().getId(),
                        cupom.getUsuario().getNomeCompleto(),
                        cupom.getUsuario().getFotoUrl())
        );
    }
}
