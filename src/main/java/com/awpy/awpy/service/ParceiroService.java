package com.awpy.awpy.service;

import com.awpy.awpy.dto.parceiro.ParceiroCadastroRequest;
import com.awpy.awpy.dto.parceiro.ParceiroResponse;
import com.awpy.awpy.model.Parceiro;
import com.awpy.awpy.repository.ParceiroRepository;
import com.awpy.awpy.service.exception.RecursoNaoEncontradoException;
import com.awpy.awpy.service.exception.RegraNegocioException;
import com.awpy.awpy.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ParceiroService {

    private static final String SUBPASTA_FOTOS = "parceiros";

    private final ParceiroRepository parceiroRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Transactional
    public ParceiroResponse cadastrar(ParceiroCadastroRequest request) {
        if (parceiroRepository.existsByEmail(request.email())) {
            throw new RegraNegocioException("e-mail já cadastrado");
        }

        Parceiro parceiro = Parceiro.builder()
                .nomeEstabelecimento(request.nomeEstabelecimento())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .percentualDesconto(request.percentualDesconto())
                .percentualCashback(request.percentualCashback())
                .ativo(true)
                .build();

        return ParceiroResponse.fromEntity(parceiroRepository.save(parceiro));
    }

    /** Foto do parceiro é gerenciada pelo admin/funcionário, nunca pelo próprio parceiro (regra do PDF). */
    @Transactional
    public ParceiroResponse atualizarFoto(Long parceiroId, MultipartFile foto) {
        Parceiro parceiro = parceiroRepository.findById(parceiroId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("parceiro não encontrado"));

        String fotoUrl = fileStorageService.salvar(foto, SUBPASTA_FOTOS);
        parceiro.setFotoUrl(fotoUrl);

        return ParceiroResponse.fromEntity(parceiroRepository.save(parceiro));
    }
}
