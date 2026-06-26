package com.awpy.awpy.storage;

import com.awpy.awpy.service.exception.RegraNegocioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Guarda fotos enviadas (usuário/parceiro) no disco local, fora do classpath
 * (src/main/resources), porque conteúdo enviado pelo usuário não deve viver junto
 * do código-fonte nem exigir rebuild para aparecer. Cada foto recebe um nome novo
 * (UUID) para não colidir e para não expor o nome original do arquivo do cliente.
 */
@Service
public class FileStorageService {

    private static final Set<String> CONTENT_TYPES_PERMITIDOS = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Map<String, String> EXTENSAO_POR_CONTENT_TYPE = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");

    private final Path diretorioBase;

    public FileStorageService(@Value("${awpy.upload.dir:uploads}") String diretorioConfigurado) {
        this.diretorioBase = Path.of(diretorioConfigurado).toAbsolutePath().normalize();
        try {
            Files.createDirectories(diretorioBase);
        } catch (IOException e) {
            throw new UncheckedIOException("não foi possível criar o diretório de uploads", e);
        }
    }

    /**
     * @param subpasta separa fotos de usuário e de parceiro em pastas diferentes
     * @return URL pública relativa (servida por WebConfig em /uploads/**)
     */
    public String salvar(MultipartFile arquivo, String subpasta) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RegraNegocioException("arquivo de foto vazio");
        }

        String contentType = arquivo.getContentType();
        if (contentType == null || !CONTENT_TYPES_PERMITIDOS.contains(contentType)) {
            throw new RegraNegocioException("formato de imagem não suportado (use JPEG, PNG ou WEBP)");
        }

        String nomeArquivo = UUID.randomUUID() + EXTENSAO_POR_CONTENT_TYPE.get(contentType);

        try {
            Path diretorioDestino = diretorioBase.resolve(subpasta);
            Files.createDirectories(diretorioDestino);

            Path destino = diretorioDestino.resolve(nomeArquivo);
            arquivo.transferTo(destino.toFile());

            return "/uploads/" + subpasta + "/" + nomeArquivo;
        } catch (IOException e) {
            throw new UncheckedIOException("falha ao salvar a foto", e);
        }
    }
}
