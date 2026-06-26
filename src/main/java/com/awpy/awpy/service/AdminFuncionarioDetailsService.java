package com.awpy.awpy.service;

import com.awpy.awpy.model.AdminFuncionario;
import com.awpy.awpy.repository.AdminFuncionarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminFuncionarioDetailsService implements UserDetailsService {

    private final AdminFuncionarioRepository adminFuncionarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AdminFuncionario admin = adminFuncionarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("administrador/funcionário não encontrado"));

        return User.builder()
                .username(admin.getEmail())
                .password(admin.getSenha())
                .disabled(!admin.getAtivo())
                .roles(admin.getNivelPermissao().name())
                .build();
    }
}
