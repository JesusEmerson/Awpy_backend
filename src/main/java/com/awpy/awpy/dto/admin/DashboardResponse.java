package com.awpy.awpy.dto.admin;

import java.util.List;

public record DashboardResponse(
        long totalUsuarios,
        long totalParceiros,
        long totalCuponsAtivos,
        double totalKgRecicladoGeral,
        long totalPontosDistribuidos,
        List<RankingUsuarioKg> rankingUsuarios,
        List<RankingParceiroKg> rankingParceiros
) {
    public record RankingUsuarioKg(Long usuarioId, String nome, Double totalKgEntregue) {
    }

    public record RankingParceiroKg(Long parceiroId, String nomeEstabelecimento, Double totalKgRecolhido) {
    }
}
