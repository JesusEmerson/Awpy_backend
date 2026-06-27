package com.awpy.awpy.service;

import com.awpy.awpy.dto.auth.LoginResponse;
import com.awpy.awpy.dto.usuario.UsuarioCadastroRequest;
import com.awpy.awpy.dto.usuario.UsuarioResponse;
import com.awpy.awpy.model.HistoricoPontos;
import com.awpy.awpy.model.Usuario;
import com.awpy.awpy.repository.HistoricoPontosRepository;
import com.awpy.awpy.repository.UsuarioRepository;
import com.awpy.awpy.security.JwtService;
import com.awpy.awpy.service.exception.RecursoNaoEncontradoException;
import com.awpy.awpy.service.exception.RegraNegocioException;
import com.awpy.awpy.storage.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private HistoricoPontosRepository historicoPontosRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioCadastroRequest request() {
        return new UsuarioCadastroRequest(
                "Usuario Teste", "12345678901", "usuario@teste.com", "senha1234",
                "11999999999", "Rua A, 123", "12345678");
    }

    @Test
    void cadastraComSucessoEArmazenaSenhaComHash() {
        when(usuarioRepository.existsByEmail("usuario@teste.com")).thenReturn(false);
        when(usuarioRepository.existsByCpfCnpj("12345678901")).thenReturn(false);
        when(passwordEncoder.encode("senha1234")).thenReturn("hash-da-senha");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.gerarToken("usuario@teste.com", "USUARIO")).thenReturn("token-fake");

        LoginResponse<UsuarioResponse> response = usuarioService.cadastrar(request());

        assertThat(response.token()).isEqualTo("token-fake");
        assertThat(response.role()).isEqualTo("USUARIO");
        assertThat(response.perfil().email()).isEqualTo("usuario@teste.com");
        assertThat(response.perfil().saldoPontos()).isZero();
        verify(usuarioRepository).save(argThatSenhaEhHash());
    }

    @Test
    void falhaQuandoEmailJaCadastrado() {
        when(usuarioRepository.existsByEmail("usuario@teste.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.cadastrar(request()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("e-mail já cadastrado");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void falhaQuandoCpfCnpjJaCadastrado() {
        when(usuarioRepository.existsByEmail("usuario@teste.com")).thenReturn(false);
        when(usuarioRepository.existsByCpfCnpj("12345678901")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.cadastrar(request()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("CPF/CNPJ já cadastrado");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void creditarPontosSomaAoSaldoEGravaHistorico() {
        Usuario usuario = Usuario.builder().id(1L).email("usuario@teste.com").saldoPontos(100L).build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        UsuarioResponse response = usuarioService.creditarPontos(1L, 50L);

        assertThat(response.saldoPontos()).isEqualTo(150L);
        verify(usuarioRepository).save(usuario);
        verify(historicoPontosRepository).save(argThatRegistraPontosCorretos());
    }

    @Test
    void creditarPontosFalhaSeUsuarioNaoExiste() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.creditarPontos(1L, 50L))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        verify(historicoPontosRepository, never()).save(any());
    }

    @Test
    void atualizarFotoSalvaArquivoEAtualizaUsuario() {
        Usuario usuario = Usuario.builder().id(1L).email("usuario@teste.com").build();
        MockMultipartFile foto = new MockMultipartFile("foto", "foto.jpg", "image/jpeg", new byte[]{1});

        when(usuarioRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(usuario));
        when(fileStorageService.salvar(foto, "usuarios")).thenReturn("/uploads/usuarios/abc.jpg");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        UsuarioResponse response = usuarioService.atualizarFoto("usuario@teste.com", foto);

        assertThat(response.fotoUrl()).isEqualTo("/uploads/usuarios/abc.jpg");
    }

    @Test
    void atualizarFotoFalhaSeUsuarioAutenticadoNaoExiste() {
        MockMultipartFile foto = new MockMultipartFile("foto", "foto.jpg", "image/jpeg", new byte[]{1});

        when(usuarioRepository.findByEmail("fantasma@teste.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.atualizarFoto("fantasma@teste.com", foto))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        verify(fileStorageService, never()).salvar(any(), any());
    }

    private Usuario argThatSenhaEhHash() {
        return org.mockito.ArgumentMatchers.argThat(usuario -> "hash-da-senha".equals(usuario.getSenha()));
    }

    private HistoricoPontos argThatRegistraPontosCorretos() {
        return org.mockito.ArgumentMatchers.argThat(historico -> historico.getPontos().equals(50L));
    }
}
