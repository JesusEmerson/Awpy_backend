package com.awpy.awpy.dto.ranking;

import com.awpy.awpy.repository.RankingMensalProjection;

public record RankingResponse(
        int posicao,
        Long usuarioId,
        String nomeCompleto,
        Long pontosNoMes
) {
    public static RankingResponse fromProjection(RankingMensalProjection projection, int posicao) {
        return new RankingResponse(
                posicao, projection.getUsuarioId(), projection.getNomeCompleto(), projection.getTotalPontos());
    }
}
