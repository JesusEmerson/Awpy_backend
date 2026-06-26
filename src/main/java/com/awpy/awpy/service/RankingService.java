package com.awpy.awpy.service;

import com.awpy.awpy.dto.ranking.RankingResponse;
import com.awpy.awpy.repository.HistoricoPontosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
        LocalDateTime inicioDoMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        var projecoes = historicoPontosRepository.rankingDesde(inicioDoMes, PageRequest.of(0, TOP));

        List<RankingResponse> ranking = new ArrayList<>();
        for (int i = 0; i < projecoes.size(); i++) {
            ranking.add(RankingResponse.fromProjection(projecoes.get(i), i + 1));
        }

        return ranking;
    }
}
