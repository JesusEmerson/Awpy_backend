package com.awpy.awpy.service;

import com.awpy.awpy.dto.ranking.RankingResponse;
import com.awpy.awpy.repository.HistoricoPontosRepository;
import com.awpy.awpy.repository.RankingMensalProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private HistoricoPontosRepository historicoPontosRepository;

    @InjectMocks
    private RankingService rankingService;

    @Test
    void montaPosicoesNaOrdemRetornadaPelaConsulta() {
        RankingMensalProjection primeiro = projecao(1L, "Usuario A", 300L);
        RankingMensalProjection segundo = projecao(2L, "Usuario B", 200L);

        when(historicoPontosRepository.rankingDesde(any(), any(Pageable.class)))
                .thenReturn(List.of(primeiro, segundo));

        List<RankingResponse> ranking = rankingService.topCinco();

        assertThat(ranking).hasSize(2);
        assertThat(ranking.get(0).posicao()).isEqualTo(1);
        assertThat(ranking.get(0).usuarioId()).isEqualTo(1L);
        assertThat(ranking.get(0).pontosNoMes()).isEqualTo(300L);
        assertThat(ranking.get(1).posicao()).isEqualTo(2);
    }

    @Test
    void retornaListaVaziaQuandoNaoHaPontosNoMes() {
        when(historicoPontosRepository.rankingDesde(any(), any(Pageable.class))).thenReturn(List.of());

        assertThat(rankingService.topCinco()).isEmpty();
    }

    @Test
    void minhaPosicaoEncontraUsuarioNoMeioDaLista() {
        RankingMensalProjection primeiro = projecao(1L, "Usuario A", 300L);
        RankingMensalProjection segundo = projecao(2L, "Usuario B", 200L);
        RankingMensalProjection terceiro = projecao(3L, "Usuario C", 100L);

        when(historicoPontosRepository.rankingDesde(any(), any(Pageable.class)))
                .thenReturn(List.of(primeiro, segundo, terceiro));

        assertThat(rankingService.minhaPosicao(2L)).isEqualTo(2);
    }

    @Test
    void minhaPosicaoRetornaNullSeUsuarioNaoPontuouNoMes() {
        RankingMensalProjection primeiro = projecao(1L, "Usuario A", 300L);

        when(historicoPontosRepository.rankingDesde(any(), any(Pageable.class)))
                .thenReturn(List.of(primeiro));

        assertThat(rankingService.minhaPosicao(99L)).isNull();
    }

    private RankingMensalProjection projecao(Long usuarioId, String nome, Long total) {
        return new RankingMensalProjection() {
            @Override
            public Long getUsuarioId() {
                return usuarioId;
            }

            @Override
            public String getNomeCompleto() {
                return nome;
            }

            @Override
            public Long getTotalPontos() {
                return total;
            }
        };
    }
}
