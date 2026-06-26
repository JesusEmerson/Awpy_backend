package com.awpy.awpy.config;

import com.awpy.awpy.model.AdminFuncionario;
import com.awpy.awpy.model.enums.NivelPermissao;
import com.awpy.awpy.repository.AdminFuncionarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Não existe endpoint público para criar o primeiro administrador (cadastro de
 * admin/funcionário exige um ADMINISTRADOR autenticado). Para resolver esse problema
 * de "ovo e galinha", este runner cria um administrador inicial na primeira vez que
 * a aplicação sobe sem nenhum AdminFuncionario cadastrado.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapRunner implements ApplicationRunner {

    private final AdminFuncionarioRepository adminFuncionarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${awpy.admin.bootstrap.email:admin@awpy.com}")
    private String emailBootstrap;

    @Value("${awpy.admin.bootstrap.senha:troque-esta-senha}")
    private String senhaBootstrap;

    @Override
    public void run(ApplicationArguments args) {
        if (adminFuncionarioRepository.count() > 0) {
            return;
        }

        AdminFuncionario admin = AdminFuncionario.builder()
                .nomeCompleto("Administrador Inicial")
                .email(emailBootstrap)
                .senha(passwordEncoder.encode(senhaBootstrap))
                .nivelPermissao(NivelPermissao.ADMINISTRADOR)
                .ativo(true)
                .build();

        adminFuncionarioRepository.save(admin);
        log.warn("Administrador inicial criado com e-mail '{}'. Troque a senha padrão imediatamente.", emailBootstrap);
    }
}
