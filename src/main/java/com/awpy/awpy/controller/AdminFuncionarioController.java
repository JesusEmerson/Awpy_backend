package com.awpy.awpy.controller;

import com.awpy.awpy.dto.admin.AdminFuncionarioCadastroRequest;
import com.awpy.awpy.dto.admin.AdminFuncionarioLoginRequest;
import com.awpy.awpy.dto.admin.AdminFuncionarioResponse;
import com.awpy.awpy.dto.auth.LoginResponse;
import com.awpy.awpy.repository.AdminFuncionarioRepository;
import com.awpy.awpy.security.AuthorityUtils;
import com.awpy.awpy.security.JwtService;
import com.awpy.awpy.service.AdminFuncionarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminFuncionarioController {

    private final AdminFuncionarioService adminFuncionarioService;
    private final AuthenticationManager authenticationManager;
    private final AdminFuncionarioRepository adminFuncionarioRepository;
    private final JwtService jwtService;

    @PostMapping("/cadastro")
    public ResponseEntity<AdminFuncionarioResponse> cadastrar(
            @Valid @RequestBody AdminFuncionarioCadastroRequest request) {
        AdminFuncionarioResponse response = adminFuncionarioService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public LoginResponse<AdminFuncionarioResponse> login(@Valid @RequestBody AdminFuncionarioLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

        // admin/funcionário compartilham login, mas o papel real precisa vir do
        // próprio registro (nivelPermissao), não de uma constante fixa como nos
        // outros dois controllers.
        AdminFuncionarioResponse admin = adminFuncionarioRepository.findByEmail(request.email())
                .map(AdminFuncionarioResponse::fromEntity)
                .orElseThrow();

        String role = admin.nivelPermissao().name();
        if (!AuthorityUtils.temPapel(authentication, role)) {
            throw new BadCredentialsException("credenciais inválidas para administrador/funcionário");
        }

        String token = jwtService.gerarToken(request.email(), role);
        return new LoginResponse<>(token, admin);
    }
}
