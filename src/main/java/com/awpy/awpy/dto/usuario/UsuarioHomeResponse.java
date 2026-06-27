package com.awpy.awpy.dto.usuario;

import com.awpy.awpy.dto.ranking.RankingResponse;
import com.awpy.awpy.model.Usuario;

import java.util.List;

public record UsuarioHomeResponse(
        String nomeCompleto,
        String fotoUrl,
        Long saldoPontos,
        Double totalKgReciclado,
        String qrCodeUsuario,
        Integer minhaPosicaoRanking,
        List<RankingResponse> ranking
) {
    public static UsuarioHomeResponse montar(
            Usuario usuario, Double totalKgReciclado, Integer minhaPosicaoRanking, List<RankingResponse> ranking) {
        return new UsuarioHomeResponse(
                usuario.getNomeCompleto(),
                usuario.getFotoUrl(),
                usuario.getSaldoPontos(),
                totalKgReciclado,
                usuario.getQrCodeUsuario(),
                minhaPosicaoRanking,
                ranking
        );
    }
}
