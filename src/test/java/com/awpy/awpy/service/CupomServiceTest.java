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
import org.springframework.security.access.AccessDeniedException;

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
 * confirmar/cancelar um cupom. Cobrem tanto as regras descritas no PDF (saldo,
 * benefício ativo, 1 cupom ativo por vez, expiração, já utilizado) quanto a
 * correção de IDOR (o id da URL não pode ser aceito sem checar contra o e-mail
 * autenticado).
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
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(beneficioRepository.findById(1L)).thenReturn(Optional.of(beneficio));
        when(cupomRepository.existsByUsuarioAndStatus(usuario, StatusCupom.ATIVO)).thenReturn(false);
        when(cupomRepository.save(any(Cupom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CupomResponse response = cupomService.resgatar(1L, 1L, "usuario@teste.com");

        assertThat(response.status()).isEqualTo(StatusCupom.ATIVO);
        assertThat(response.beneficioNome()).isEqualTo("Café grátis");
    }

    @Test
    void resgatarFalhaSeUsuarioAutenticadoNaoForOTitularDoId() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> cupomService.resgatar(1L, 1L, "outra-pessoa@teste.com"))
                .isInstanceOf(AccessDeniedException.class);

        verify(cupomRepository, never()).save(any());
    }

    @Test
    void resgatarFalhaSeBeneficioInativo() {
        beneficio.setAtivo(false);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(beneficioRepository.findById(1L)).thenReturn(Optional.of(beneficio));

        assertThatThrownBy(() -> cupomService.resgatar(1L, 1L, "usuario@teste.com"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("indisponível");
    }

    @Test
    void resgatarFalhaSeSaldoInsuficiente() {
        usuario.setSaldoPontos(50L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(beneficioRepository.findById(1L)).thenReturn(Optional.of(beneficio));

        assertThatThrownBy(() -> cupomService.resgatar(1L, 1L, "usuario@teste.com"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("saldo");
    }

    @Test
    void resgatarFalhaSeJaExisteCupomAtivo() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(beneficioRepository.findById(1L)).thenReturn(Optional.of(beneficio));
        when(cupomRepository.existsByUsuarioAndStatus(usuario, StatusCupom.ATIVO)).thenReturn(true);

        assertThatThrownBy(() -> cupomService.resgatar(1L, 1L, "usuario@teste.com"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("já possui um cupom ativo");
    }

    // ---------- buscarCupomAtivo ----------

    @Test
    void buscarCupomAtivoFalhaSeUsuarioAutenticadoNaoForOTitular() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> cupomService.buscarCupomAtivo(1L, "outra-pessoa@teste.com"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void buscarCupomAtivoRetorna404QuandoNaoHaCupomAtivo() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(cupomRepository.findByUsuarioAndStatus(usuario, StatusCupom.ATIVO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cupomService.buscarCupomAtivo(1L, "usuario@teste.com"))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // ---------- validação do parceiro (scanner) ----------

    @Test
    void buscarParaValidacaoFalhaSeParceiroAutenticadoNaoForOTitularDoId() {
        when(parceiroRepository.findById(1L)).thenReturn(Optional.of(parceiro));

        assertThatThrownBy(() -> cupomService.buscarParaValidacao(1L, "qr-123", "outro-parceiro@teste.com"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void buscarParaValidacaoFalhaSeCupomNaoPertenceAoParceiro() {
        Parceiro outroParceiro = Parceiro.builder().id(2L).email("outro@teste.com").build();
        Cupom cupom = cupomAtivo(outroParceiro);

        when(parceiroRepository.findById(1L)).thenReturn(Optional.of(parceiro));
        when(cupomRepository.findByQrCodeUnico("qr-123")).thenReturn(Optional.of(cupom));

        assertThatThrownBy(() -> cupomService.buscarParaValidacao(1L, "qr-123", "parceiro@teste.com"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não pertence ao parceiro");
    }

    @Test
    void buscarParaValidacaoFalhaSeCupomJaUtilizado() {
        Cupom cupom = cupomAtivo(parceiro);
        cupom.setStatus(StatusCupom.UTILIZADO);

        when(parceiroRepository.findById(1L)).thenReturn(Optional.of(parceiro));
        when(cupomRepository.findByQrCodeUnico("qr-123")).thenReturn(Optional.of(cupom));

        assertThatThrownBy(() -> cupomService.buscarParaValidacao(1L, "qr-123", "parceiro@teste.com"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("já utilizado");
    }

    @Test
    void buscarParaValidacaoFalhaSeCupomExpirado() {
        Cupom cupom = cupomAtivo(parceiro);
        cupom.setDataExpiracao(LocalDateTime.now().minusDays(1));

        when(parceiroRepository.findById(1L)).thenReturn(Optional.of(parceiro));
        when(cupomRepository.findByQrCodeUnico("qr-123")).thenReturn(Optional.of(cupom));
        lenient().when(cupomRepository.save(any(Cupom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> cupomService.buscarParaValidacao(1L, "qr-123", "parceiro@teste.com"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("expirado");
    }

    // ---------- confirmarUso ----------

    @Test
    void confirmarUsoDebitaPontosEMudaStatusParaUtilizado() {
        Cupom cupom = cupomAtivo(parceiro);

        when(parceiroRepository.findById(1L)).thenReturn(Optional.of(parceiro));
        when(cupomRepository.findByQrCodeUnico("qr-123")).thenReturn(Optional.of(cupom));
        when(cupomRepository.save(any(Cupom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CardValidacaoResponse response = cupomService.confirmarUso(1L, "qr-123", "parceiro@teste.com");

        assertThat(response.status()).isEqualTo(StatusCupom.UTILIZADO);
        assertThat(usuario.getSaldoPontos()).isEqualTo(50L);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void confirmarUsoFalhaSeParceiroAutenticadoNaoForOTitularDoId() {
        when(parceiroRepository.findById(1L)).thenReturn(Optional.of(parceiro));

        assertThatThrownBy(() -> cupomService.confirmarUso(1L, "qr-123", "outro-parceiro@teste.com"))
                .isInstanceOf(AccessDeniedException.class);

        verify(cupomRepository, never()).findByQrCodeUnico(any());
        verify(usuarioRepository, never()).save(any());
    }

    // ---------- cancelar ----------

    @Test
    void cancelarNaoAlteraSaldoNemStatusDoCupom() {
        Cupom cupom = cupomAtivo(parceiro);

        when(parceiroRepository.findById(1L)).thenReturn(Optional.of(parceiro));
        when(cupomRepository.findByQrCodeUnico("qr-123")).thenReturn(Optional.of(cupom));

        CardValidacaoResponse response = cupomService.cancelar(1L, "qr-123", "parceiro@teste.com");

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
