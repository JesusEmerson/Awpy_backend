package com.awpy.awpy.service;

import com.awpy.awpy.dto.admin.DashboardResponse;
import com.awpy.awpy.model.enums.StatusCupom;
import com.awpy.awpy.repository.CupomRepository;
import com.awpy.awpy.repository.HistoricoPontosRepository;
import com.awpy.awpy.repository.ParceiroRepository;
import com.awpy.awpy.repository.ReciclagemRepository;
import com.awpy.awpy.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int TOP = 5;

    private final UsuarioRepository usuarioRepository;
    private final ParceiroRepository parceiroRepository;
    private final CupomRepository cupomRepository;
    private final ReciclagemRepository reciclagemRepository;
    private final HistoricoPontosRepository historicoPontosRepository;

    public DashboardResponse obter() {
        var rankingUsuarios = reciclagemRepository.rankingUsuariosPorKg(PageRequest.of(0, TOP)).stream()
                .map(p -> new DashboardResponse.RankingUsuarioKg(p.getUsuarioId(), p.getNome(), p.getTotalKg()))
                .toList();

        var rankingParceiros = reciclagemRepository.rankingParceirosPorKg(PageRequest.of(0, TOP)).stream()
                .map(p -> new DashboardResponse.RankingParceiroKg(
                        p.getParceiroId(), p.getNomeEstabelecimento(), p.getTotalKg()))
                .toList();

        return new DashboardResponse(
                usuarioRepository.count(),
                parceiroRepository.count(),
                cupomRepository.countByStatus(StatusCupom.ATIVO),
                reciclagemRepository.somarTodosOsQuilos(),
                historicoPontosRepository.somarTodosOsPontos(),
                rankingUsuarios,
                rankingParceiros
        );
    }
}
