package com.awpy.awpy.storage;

import com.awpy.awpy.service.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    @TempDir
    Path diretorioTemporario;

    private FileStorageService fileStorageService() {
        return new FileStorageService(diretorioTemporario.toString());
    }

    @Test
    void salvaImagemValidaERetornaUrlPublica() {
        MockMultipartFile arquivo = new MockMultipartFile("foto", "minha-foto.jpg", "image/jpeg", new byte[]{1, 2, 3});

        String url = fileStorageService().salvar(arquivo, "usuarios");

        assertThat(url).startsWith("/uploads/usuarios/").endsWith(".jpg");
        Path arquivoSalvo = diretorioTemporario.resolve(url.replaceFirst("^/uploads/", ""));
        assertThat(Files.exists(arquivoSalvo)).isTrue();
    }

    @Test
    void rejeitaContentTypeNaoSuportado() {
        MockMultipartFile arquivo = new MockMultipartFile("foto", "arquivo.pdf", "application/pdf", new byte[]{1});

        assertThatThrownBy(() -> fileStorageService().salvar(arquivo, "usuarios"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("formato de imagem não suportado");
    }

    @Test
    void rejeitaArquivoVazio() {
        MockMultipartFile arquivo = new MockMultipartFile("foto", "vazio.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> fileStorageService().salvar(arquivo, "usuarios"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("vazio");
    }

    @Test
    void geraNomesDiferentesParaDoisUploadsDoMesmoArquivo() {
        FileStorageService service = fileStorageService();
        MockMultipartFile arquivo1 = new MockMultipartFile("foto", "a.png", "image/png", new byte[]{1});
        MockMultipartFile arquivo2 = new MockMultipartFile("foto", "a.png", "image/png", new byte[]{1});

        String url1 = service.salvar(arquivo1, "parceiros");
        String url2 = service.salvar(arquivo2, "parceiros");

        assertThat(url1).isNotEqualTo(url2);
    }
}
