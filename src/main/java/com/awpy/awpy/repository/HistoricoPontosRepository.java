package com.awpy.awpy.repository;

import com.awpy.awpy.model.HistoricoPontos;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HistoricoPontosRepository extends JpaRepository<HistoricoPontos, Long> {

    @Query("""
            SELECT h.usuario.id AS usuarioId, h.usuario.nomeCompleto AS nomeCompleto, SUM(h.pontos) AS totalPontos
            FROM HistoricoPontos h
            WHERE h.dataHora >= :inicioDoMes
            GROUP BY h.usuario.id, h.usuario.nomeCompleto
            ORDER BY SUM(h.pontos) DESC
            """)
    List<RankingMensalProjection> rankingDesde(@Param("inicioDoMes") LocalDateTime inicioDoMes, Pageable pageable);

    @Query("SELECT COALESCE(SUM(h.pontos), 0) FROM HistoricoPontos h")
    Long somarTodosOsPontos();
}
