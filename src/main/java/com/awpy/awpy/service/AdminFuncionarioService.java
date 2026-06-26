package com.awpy.awpy.service;

import com.awpy.awpy.dto.admin.AdminFuncionarioCadastroRequest;
import com.awpy.awpy.dto.admin.AdminFuncionarioResponse;
import com.awpy.awpy.model.AdminFuncionario;
import com.awpy.awpy.repository.AdminFuncionarioRepository;
import com.awpy.awpy.service.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminFuncionarioService {

    private final AdminFuncionarioRepository adminFuncionarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AdminFuncionarioResponse cadastrar(AdminFuncionarioCadastroRequest request) {
        if (adminFuncionarioRepository.existsByEmail(request.email())) {
            throw new RegraNegocioException("e-mail já cadastrado");
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
}
