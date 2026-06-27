package com.awpy.awpy.service;

import com.awpy.awpy.dto.cupom.CardValidacaoResponse;
import com.awpy.awpy.dto.cupom.CupomResponse;
import com.awpy.awpy.model.Beneficio;
import com.awpy.awpy.model.Cupom;
import com.awpy.awpy.model.Parceiro;
import com.awpy.awpy.model.Usuario;
import com.awpy.awpy.model.enums.StatusCupom;
import com.awpy.awpy.repository.BeneficioRepository;
import com.awpy.awpy.repository.CupomRepository;
import com.awpy.awpy.repository.ParceiroRepository;
import com.awpy.awpy.repository.UsuarioRepository;
import com.awpy.awpy.service.exception.RecursoNaoEncontradoException;
import com.awpy.awpy.service.exception.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes da regra de negócio mais sensível do projeto: quem pode resgatar, ver e
 * confirmar/cancelar um cupom. A identidade é sempre o e-mail autenticado (não há
 * id de path) — não existe IDOR possível por construção, então não há mais um
 * cenário de "usuário tentando acessar outro id" pra testar aqui: o serviço nem
 * aceita um id de outra pessoa, só resolve "quem está logado".
 */
@ExtendWith(MockitoExtension.class)
class CupomServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private BeneficioRepository beneficioRepository;
    @Mock
    private CupomRepository cupomRepository;
    @Mock
    private ParceiroRepository parceiroRepository;

    @InjectMocks
    private CupomService cupomService;

    private Usuario usuario;
    private Parceiro parceiro;
    private Beneficio beneficio;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nomeCompleto("Usuario Teste")
                .email("usuario@teste.com")
                .saldoPontos(150L)
                .build();

        parceiro = Parceiro.builder()
                .id(1L)
                .nomeEstabelecimento("Parceiro Teste")
                .email("parceiro@teste.com")
                .ativo(true)
                .build();

        beneficio = Beneficio.builder()
                .id(1L)
                .nome("Café grátis")
                .custoEmPontos(100L)
                .ativo(true)
                .parceiro(parceiro)
                .build();
    }

    // ---------- resgatar ----------

    @Test
    void resgatarComSucessoQuandoTudoValido() {
        when(usuarioRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(usuario));
        when(beneficioRepository.findById(1L)).thenReturn(Optional.of(beneficio));
        when(cupomRepository.countByUsuarioAndStatus(usuario, StatusCupom.ATIVO)).thenReturn(0L);
        when(cupomRepository.save(any(Cupom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CupomResponse response = cupomService.resgatar("usuario@teste.com", 1L);

        assertThat(response.status()).isEqualTo(StatusCupom.ATIVO);
        assertThat(response.beneficioNome()).isEqualTo("Café grátis");
    }

    @Test
    void resgatarFalhaSeUsuarioAutenticadoNaoExiste() {
        when(usuarioRepository.findByEmail("fantasma@teste.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cupomService.resgatar("fantasma@teste.com", 1L))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        verify(cupomRepository, never()).save(any());
    }

    @Test
    void resgatarFalhaSeBeneficioInativo() {
        beneficio.setAtivo(false);
        when(usuarioRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(usuario));
        when(beneficioRepository.findById(1L)).thenReturn(Optional.of(beneficio));

        assertThatThrownBy(() -> cupomService.resgatar("usuario@teste.com", 1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("indisponível");
    }

    @Test
    void resgatarFalhaSeSaldoInsuficiente() {
        usuario.setSaldoPontos(50L);
        when(usuarioRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(usuario));
        when(beneficioRepository.findById(1L)).thenReturn(Optional.of(beneficio));

        assertThatThrownBy(() -> cupomService.resgatar("usuario@teste.com", 1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("saldo");
    }

    @Test
    void resgatarFalhaSeJaAtingiuLimiteDeCuponsAtivos() {
        when(usuarioRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(usuario));
        when(beneficioRepository.findById(1L)).thenReturn(Optional.of(beneficio));
        when(cupomRepository.countByUsuarioAndStatus(usuario, StatusCupom.ATIVO)).thenReturn(3L);

        assertThatThrownBy(() -> cupomService.resgatar("usuario@teste.com", 1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("limite");
    }

    // ---------- buscarCuponsAtivos ----------

    @Test
    void buscarCuponsAtivosRetornaListaVaziaQuandoNaoHaNenhumAtivo() {
        when(usuarioRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(usuario));
        when(cupomRepository.findByUsuarioAndStatus(usuario, StatusCupom.ATIVO)).thenReturn(java.util.List.of());

        assertThat(cupomService.buscarCuponsAtivos("usuario@teste.com")).isEmpty();
    }

    @Test
    void buscarCuponsAtivosRetornaTodosOsCuponsAtivosDoUsuario() {
        Cupom cupom1 = cupomAtivo(parceiro);
        Cupom cupom2 = cupomAtivo(parceiro);
        when(usuarioRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(usuario));
        when(cupomRepository.findByUsuarioAndStatus(usuario, StatusCupom.ATIVO))
                .thenReturn(java.util.List.of(cupom1, cupom2));

        assertThat(cupomService.buscarCuponsAtivos("usuario@teste.com")).hasSize(2);
    }

    // ---------- validação do parceiro (scanner) ----------

    @Test
    void buscarParaValidacaoFalhaSeParceiroAutenticadoNaoExiste() {
        when(parceiroRepository.findByEmail("fantasma@teste.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cupomService.buscarParaValidacao("fantasma@teste.com", "qr-123"))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void buscarParaValidacaoFalhaSeCupomNaoPertenceAoParceiro() {
        Parceiro outroParceiro = Parceiro.builder().id(2L).email("outro@teste.com").build();
        Cupom cupom = cupomAtivo(outroParceiro);

        when(parceiroRepository.findByEmail("parceiro@teste.com")).thenReturn(Optional.of(parceiro));
        when(cupomRepository.findByQrCodeUnico("qr-123")).thenReturn(Optional.of(cupom));

        assertThatThrownBy(() -> cupomService.buscarParaValidacao("parceiro@teste.com", "qr-123"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não pertence ao parceiro");
    }

    @Test
    void buscarParaValidacaoFalhaSeCupomJaUtilizado() {
        Cupom cupom = cupomAtivo(parceiro);
        cupom.setStatus(StatusCupom.UTILIZADO);

        when(parceiroRepository.findByEmail("parceiro@teste.com")).thenReturn(Optional.of(parceiro));
        when(cupomRepository.findByQrCodeUnico("qr-123")).thenReturn(Optional.of(cupom));

        assertThatThrownBy(() -> cupomService.buscarParaValidacao("parceiro@teste.com", "qr-123"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("já utilizado");
    }

    @Test
    void buscarParaValidacaoFalhaSeCupomExpirado() {
        Cupom cupom = cupomAtivo(parceiro);
        cupom.setDataExpiracao(LocalDateTime.now().minusDays(1));

        when(parceiroRepository.findByEmail("parceiro@teste.com")).thenReturn(Optional.of(parceiro));
        when(cupomRepository.findByQrCodeUnico("qr-123")).thenReturn(Optional.of(cupom));
        lenient().when(cupomRepository.save(any(Cupom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> cupomService.buscarParaValidacao("parceiro@teste.com", "qr-123"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("expirado");
    }

    // ---------- confirmarUso ----------

    @Test
    void confirmarUsoDebitaPontosEMudaStatusParaUtilizado() {
        Cupom cupom = cupomAtivo(parceiro);

        when(parceiroRepository.findByEmail("parceiro@teste.com")).thenReturn(Optional.of(parceiro));
        when(cupomRepository.findByQrCodeUnico("qr-123")).thenReturn(Optional.of(cupom));
        when(cupomRepository.save(any(Cupom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CardValidacaoResponse response = cupomService.confirmarUso("parceiro@teste.com", "qr-123");

        assertThat(response.status()).isEqualTo(StatusCupom.UTILIZADO);
        assertThat(usuario.getSaldoPontos()).isEqualTo(50L);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void confirmarUsoFalhaSeParceiroAutenticadoNaoExiste() {
        when(parceiroRepository.findByEmail("fantasma@teste.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cupomService.confirmarUso("fantasma@teste.com", "qr-123"))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        verify(cupomRepository, never()).findByQrCodeUnico(any());
        verify(usuarioRepository, never()).save(any());
    }

    // ---------- cancelar ----------

    @Test
    void cancelarNaoAlteraSaldoNemStatusDoCupom() {
        Cupom cupom = cupomAtivo(parceiro);

        when(parceiroRepository.findByEmail("parceiro@teste.com")).thenReturn(Optional.of(parceiro));
        when(cupomRepository.findByQrCodeUnico("qr-123")).thenReturn(Optional.of(cupom));

        CardValidacaoResponse response = cupomService.cancelar("parceiro@teste.com", "qr-123");

        assertThat(response.status()).isEqualTo(StatusCupom.ATIVO);
        assertThat(usuario.getSaldoPontos()).isEqualTo(150L);
        verify(usuarioRepository, never()).save(any());
        verify(cupomRepository, never()).save(any());
    }

    private Cupom cupomAtivo(Parceiro donoDoParceiro) {
        return Cupom.builder()
                .id(1L)
                .usuario(usuario)
                .beneficio(beneficio)
                .parceiro(donoDoParceiro)
                .qrCodeUnico("qr-123")
                .status(StatusCupom.ATIVO)
                .dataGeracao(LocalDateTime.now())
                .dataExpiracao(LocalDateTime.now().plusDays(30))
                .build();
    }
}
