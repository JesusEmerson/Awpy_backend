package com.awpy.awpy.controller;

import com.awpy.awpy.dto.admin.AdminFuncionarioResponse;
import com.awpy.awpy.dto.auth.LoginRequest;
import com.awpy.awpy.dto.auth.LoginResponse;
import com.awpy.awpy.dto.auth.RecuperarSenhaRequest;
import com.awpy.awpy.dto.auth.RecuperarSenhaResponse;
import com.awpy.awpy.dto.auth.RedefinirSenhaRequest;
import com.awpy.awpy.dto.parceiro.ParceiroResponse;
import com.awpy.awpy.dto.usuario.UsuarioResponse;
import com.awpy.awpy.repository.AdminFuncionarioRepository;
import com.awpy.awpy.repository.ParceiroRepository;
import com.awpy.awpy.repository.UsuarioRepository;
import com.awpy.awpy.security.AuthorityUtils;
import com.awpy.awpy.security.JwtService;
import com.awpy.awpy.service.RecuperacaoSenhaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login único pros três perfis (usuário, parceiro, admin/funcionário) — o app não
 * precisa saber de antemão "que tipo de conta" é aquele e-mail. O AuthenticationManager
 * (ver SecurityConfig) já tenta os três; aqui só descobrimos qual deles aceitou pra
 * montar a resposta certa e decidir a claim "role" do token.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final ParceiroRepository parceiroRepository;
    private final AdminFuncionarioRepository adminFuncionarioRepository;
    private final JwtService jwtService;
    private final RecuperacaoSenhaService recuperacaoSenhaService;

    @PostMapping("/login")
    public LoginResponse<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            System.out.println(">>> Tentativa de login para o e-mail: " + request.email());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

            System.out.println(">>> Autenticação aceita pelo Spring Security!");

            String role = AuthorityUtils.extrairPapel(authentication);
            Object perfil = carregarPerfil(role, request.email());

            String token = jwtService.gerarToken(request.email(), role);
            return new LoginResponse<>(token, role, perfil);

        } catch (Exception e) {
            System.out.println(">>> O LOGIN FALHOU PELO SEGUINTE MOTIVO:");
            e.printStackTrace(); // Isso vai jogar o erro exato no console
            throw e;
        }
    }

    @PostMapping("/recuperar-senha")
    public RecuperarSenhaResponse recuperarSenha(@Valid @RequestBody RecuperarSenhaRequest request) {
        return recuperacaoSenhaService.solicitar(request.email());
    }

    @PostMapping("/redefinir-senha")
    public void redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest request) {
        recuperacaoSenhaService.redefinir(request.email(), request.codigo(), request.novaSenha());
    }

    private Object carregarPerfil(String role, String email) {
        return switch (role) {
            case "USUARIO" -> usuarioRepository.findByEmail(email)
                    .map(UsuarioResponse::fromEntity)
                    .orElseThrow();
            case "PARCEIRO" -> parceiroRepository.findByEmail(email)
                    .map(ParceiroResponse::fromEntity)
                    .orElseThrow();
            case "ADMINISTRADOR", "FUNCIONARIO" -> adminFuncionarioRepository.findByEmail(email)
                    .map(AdminFuncionarioResponse::fromEntity)
                    .orElseThrow();
            default -> throw new BadCredentialsException("papel não reconhecido");
        };
    }
}
