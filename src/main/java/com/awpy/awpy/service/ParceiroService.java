package com.awpy.awpy.service;

import com.awpy.awpy.dto.parceiro.ParceiroCadastroRequest;
import com.awpy.awpy.dto.parceiro.ParceiroResponse;
import com.awpy.awpy.dto.parceiro.ParceiroResumoResponse;
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

import java.util.List;

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
            throw new RegraNegocioException("e-mail já cadastrado", "EMAIL_JA_CADASTRADO");
        }
        if (parceiroRepository.existsByCnpj(request.cnpj())) {
            throw new RegraNegocioException("CNPJ já cadastrado", "CNPJ_JA_CADASTRADO");
        }

        Parceiro parceiro = Parceiro.builder()
                .nomeEstabelecimento(request.nomeEstabelecimento())
                .cnpj(request.cnpj())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .telefone(request.telefone())
                .endereco(request.endereco())
                .percentualDesconto(request.percentualDesconto())
                .percentualCashback(request.percentualCashback())
                .ativo(true)
                .build();

        return ParceiroResponse.fromEntity(parceiroRepository.save(parceiro));
    }

    /** Admin/funcionário gerenciando a foto de um parceiro específico por id. */
    @Transactional
    public ParceiroResponse atualizarFoto(Long parceiroId, MultipartFile foto) {
        Parceiro parceiro = parceiroRepository.findById(parceiroId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("parceiro não encontrado"));

        return salvarFoto(parceiro, foto);
    }

    /** Parceiro autosserviço, sem id no path ("/parceiros/me/foto") — decisão revista do PDF original. */
    @Transactional
    public ParceiroResponse atualizarFotoPropria(String emailAutenticado, MultipartFile foto) {
        Parceiro parceiro = parceiroRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new RecursoNaoEncontradoException("parceiro não encontrado"));

        return salvarFoto(parceiro, foto);
    }

    private ParceiroResponse salvarFoto(Parceiro parceiro, MultipartFile foto) {
        String fotoUrl = fileStorageService.salvar(foto, SUBPASTA_FOTOS);
        parceiro.setFotoUrl(fotoUrl);

        return ParceiroResponse.fromEntity(parceiroRepository.save(parceiro));
    }

    /** Usado pra alimentar dropdown de seleção de parceiro (ex.: form de benefício). */
    public List<ParceiroResumoResponse> listar() {
        return parceiroRepository.findAll().stream()
                .map(ParceiroResumoResponse::fromEntity)
                .toList();
    }
}
