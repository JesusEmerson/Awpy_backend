package com.awpy.awpy.service;

import com.awpy.awpy.dto.admin.AdminFuncionarioCadastroRequest;
import com.awpy.awpy.dto.admin.AdminFuncionarioResponse;
import com.awpy.awpy.model.AdminFuncionario;
import com.awpy.awpy.repository.AdminFuncionarioRepository;
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
public class AdminFuncionarioService {

    private static final String SUBPASTA_FOTOS = "admins";

    private final AdminFuncionarioRepository adminFuncionarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Transactional
    public AdminFuncionarioResponse cadastrar(AdminFuncionarioCadastroRequest request) {
        if (adminFuncionarioRepository.existsByEmail(request.email())) {
            throw new RegraNegocioException("e-mail já cadastrado", "EMAIL_JA_CADASTRADO");
        }

        AdminFuncionario admin = AdminFuncionario.builder()
                .nomeCompleto(request.nomeCompleto())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .nivelPermissao(request.nivelPermissao())
                .ativo(true)
                .build();

        return AdminFuncionarioResponse.fromEntity(adminFuncionarioRepository.save(admin));
    }

    @Transactional
    public AdminFuncionarioResponse atualizarFotoPropria(String emailAutenticado, MultipartFile foto) {
        AdminFuncionario admin = adminFuncionarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new RecursoNaoEncontradoException("administrador/funcionário não encontrado"));

        String fotoUrl = fileStorageService.salvar(foto, SUBPASTA_FOTOS);
        admin.setFotoUrl(fotoUrl);

        return AdminFuncionarioResponse.fromEntity(adminFuncionarioRepository.save(admin));
    }
}
