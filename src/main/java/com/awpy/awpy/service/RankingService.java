package com.awpy.awpy.service;

import com.awpy.awpy.dto.ranking.RankingResponse;
import com.awpy.awpy.repository.HistoricoPontosRepository;
import com.awpy.awpy.repository.RankingMensalProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Ranking mensal "de verdade": soma apenas os pontos creditados (HistoricoPontos)
 * desde o primeiro dia do mês corrente, não o saldo trocável do usuário. Assim a
 * disputa do mês reseta naturalmente todo dia 1, sem nunca afetar o saldo que o
 * usuário usa para resgatar benefícios.
 */
@Service
@RequiredArgsConstructor
public class RankingService {

    private static final int TOP = 5;

    private final HistoricoPontosRepository historicoPontosRepository;

    public List<RankingResponse> topCinco() {
        var projecoes = historicoPontosRepository.rankingDesde(inicioDoMes(), PageRequest.of(0, TOP));

        List<RankingResponse> ranking = new ArrayList<>();
        for (int i = 0; i < projecoes.size(); i++) {
            ranking.add(RankingResponse.fromProjection(projecoes.get(i), i + 1));
        }

        return ranking;
    }

    /**
     * Posição do usuário no ranking mensal completo (não só o Top 5). Retorna null
     * se o usuário não pontuou nada esse mês — ele simplesmente não aparece na lista
     * (não dá pra "ser 47º de 12 pessoas que pontuaram").
     */
    public Integer minhaPosicao(Long usuarioId) {
        Pageable semLimite = PageRequest.of(0, Integer.MAX_VALUE);
        List<RankingMensalProjection> rankingCompleto = historicoPontosRepository.rankingDesde(inicioDoMes(), semLimite);

        for (int i = 0; i < rankingCompleto.size(); i++) {
            if (rankingCompleto.get(i).getUsuarioId().equals(usuarioId)) {
                return i + 1;
            }
        }

        return null;
    }

    private LocalDateTime inicioDoMes() {
        return LocalDate.now().withDayOfMonth(1).atStartOfDay();
    }
}
