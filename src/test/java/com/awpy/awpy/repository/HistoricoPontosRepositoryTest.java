package com.awpy.awpy.repository;

import com.awpy.awpy.model.HistoricoPontos;
import com.awpy.awpy.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A consulta agregada (SUM + GROUP BY + filtro de data) é a parte mais arriscada do
 * ranking mensal — esse teste prova que ela soma corretamente por usuário e ignora
 * pontos de meses anteriores.
 */
@DataJpaTest
class HistoricoPontosRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HistoricoPontosRepository historicoPontosRepository;

    @Test
    void somaPontosDoMesPorUsuarioEIgnoraMesesAnteriores() {
        Usuario usuarioA = usuarioRepository.save(usuario("a@teste.com", "11111111111"));
        Usuario usuarioB = usuarioRepository.save(usuario("b@teste.com", "22222222222"));

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime mesPassado = agora.minusMonths(2);

        historicoPontosRepository.save(historico(usuarioA, 100L, agora));
        historicoPontosRepository.save(historico(usuarioA, 50L, agora));
        historicoPontosRepository.save(historico(usuarioA, 9999L, mesPassado)); // não deve contar
        historicoPontosRepository.save(historico(usuarioB, 80L, agora));

        LocalDateTime inicioDoMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        List<RankingMensalProjection> ranking =
                historicoPontosRepository.rankingDesde(inicioDoMes, PageRequest.of(0, 5));

        assertThat(ranking).hasSize(2);
        assertThat(ranking.get(0).getUsuarioId()).isEqualTo(usuarioA.getId());
        assertThat(ranking.get(0).getTotalPontos()).isEqualTo(150L);
        assertThat(ranking.get(1).getUsuarioId()).isEqualTo(usuarioB.getId());
        assertThat(ranking.get(1).getTotalPontos()).isEqualTo(80L);
    }

    @Test
    void respeitaOLimiteDePaginacao() {
        for (int i = 0; i < 7; i++) {
            Usuario usuario = usuarioRepository.save(usuario("u" + i + "@teste.com", "1234567890" + i));
            historicoPontosRepository.save(historico(usuario, 10L * (i + 1), LocalDateTime.now()));
        }

        LocalDateTime inicioDoMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        List<RankingMensalProjection> ranking =
                historicoPontosRepository.rankingDesde(inicioDoMes, PageRequest.of(0, 5));

        assertThat(ranking).hasSize(5);
        assertThat(ranking.get(0).getTotalPontos()).isEqualTo(70L);
    }

    private Usuario usuario(String email, String cpfCnpj) {
        return Usuario.builder()
                .nomeCompleto("Usuario Teste")
                .cpfCnpj(cpfCnpj)
                .email(email)
                .senha("hash")
                .telefone("11999999999")
                .endereco("Rua A")
                .cep("12345678")
                .saldoPontos(0L)
                .build();
    }

    private HistoricoPontos historico(Usuario usuario, Long pontos, LocalDateTime dataHora) {
        return HistoricoPontos.builder()
                .usuario(usuario)
                .pontos(pontos)
                .dataHora(dataHora)
                .build();
    }
}
