package com.awpy.awpy.service;

import com.awpy.awpy.model.Parceiro;
import com.awpy.awpy.repository.ParceiroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParceiroDetailsService implements UserDetailsService {

    private final ParceiroRepository parceiroRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Parceiro parceiro = parceiroRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("parceiro não encontrado"));

        return User.builder()
                .username(parceiro.getEmail())
                .password(parceiro.getSenha())
                .disabled(!parceiro.getAtivo())
                .roles("PARCEIRO")
                .build();
    }
}
