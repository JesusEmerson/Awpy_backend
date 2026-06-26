package com.awpy.awpy.service;

import com.awpy.awpy.dto.parceiro.ParceiroCadastroRequest;
import com.awpy.awpy.dto.parceiro.ParceiroResponse;
import com.awpy.awpy.model.Parceiro;
import com.awpy.awpy.repository.ParceiroRepository;
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
class ParceiroServiceTest {

    @Mock
    private ParceiroRepository parceiroRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ParceiroService parceiroService;

    @Test
    void cadastraComSucesso() {
        ParceiroCadastroRequest request = new ParceiroCadastroRequest(
                "Parceiro Teste", "parceiro@teste.com", "senha1234", 10.0, 5.0);

        when(parceiroRepository.existsByEmail("parceiro@teste.com")).thenReturn(false);
        when(passwordEncoder.encode("senha1234")).thenReturn("hash");
        when(parceiroRepository.save(any(Parceiro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ParceiroResponse response = parceiroService.cadastrar(request);

        assertThat(response.email()).isEqualTo("parceiro@teste.com");
    }

    @Test
    void falhaQuandoEmailJaCadastrado() {
        ParceiroCadastroRequest request = new ParceiroCadastroRequest(
                "Parceiro Teste", "parceiro@teste.com", "senha1234", null, null);

        when(parceiroRepository.existsByEmail("parceiro@teste.com")).thenReturn(true);

        assertThatThrownBy(() -> parceiroService.cadastrar(request))
                .isInstanceOf(RegraNegocioException.class);

        verify(parceiroRepository, never()).save(any());
    }

    @Test
    void atualizarFotoSalvaArquivoEAtualizaParceiro() {
        Parceiro parceiro = Parceiro.builder().id(1L).email("parceiro@teste.com").build();
        MockMultipartFile foto = new MockMultipartFile("foto", "foto.png", "image/png", new byte[]{1});

        when(parceiroRepository.findById(1L)).thenReturn(Optional.of(parceiro));
        when(fileStorageService.salvar(foto, "parceiros")).thenReturn("/uploads/parceiros/abc.png");
        when(parceiroRepository.save(parceiro)).thenReturn(parceiro);

        ParceiroResponse response = parceiroService.atualizarFoto(1L, foto);

        assertThat(response.fotoUrl()).isEqualTo("/uploads/parceiros/abc.png");
    }

    @Test
    void atualizarFotoFalhaSeParceiroNaoExiste() {
        MockMultipartFile foto = new MockMultipartFile("foto", "foto.png", "image/png", new byte[]{1});
        when(parceiroRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parceiroService.atualizarFoto(1L, foto))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
