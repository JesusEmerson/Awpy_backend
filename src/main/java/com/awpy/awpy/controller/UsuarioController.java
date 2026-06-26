package com.awpy.awpy.controller;

import com.awpy.awpy.dto.auth.LoginResponse;
import com.awpy.awpy.dto.usuario.CreditoPontosRequest;
import com.awpy.awpy.dto.usuario.UsuarioCadastroRequest;
import com.awpy.awpy.dto.usuario.UsuarioLoginRequest;
import com.awpy.awpy.dto.usuario.UsuarioResponse;
import com.awpy.awpy.repository.UsuarioRepository;
import com.awpy.awpy.security.AuthorityUtils;
import com.awpy.awpy.security.JwtService;
import com.awpy.awpy.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private static final String ROLE = "USUARIO";

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody UsuarioCadastroRequest request) {
        UsuarioResponse response = usuarioService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public LoginResponse<UsuarioResponse> login(@Valid @RequestBody UsuarioLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

        if (!AuthorityUtils.temPapel(authentication, ROLE)) {
            throw new BadCredentialsException("credenciais inválidas para usuário");
        }

        UsuarioResponse usuario = usuarioRepository.findByEmail(request.email())
                .map(UsuarioResponse::fromEntity)
                .orElseThrow();

        String token = jwtService.gerarToken(request.email(), ROLE);
        return new LoginResponse<>(token, usuario);
    }

    @PostMapping("/{usuarioId}/pontos")
    public UsuarioResponse creditarPontos(
            @PathVariable Long usuarioId, @Valid @RequestBody CreditoPontosRequest request) {
        return usuarioService.creditarPontos(usuarioId, request.pontos());
    }

    @PostMapping("/{usuarioId}/foto")
    public UsuarioResponse atualizarFoto(
            @PathVariable Long usuarioId, @RequestParam("foto") MultipartFile foto, Authentication authentication) {
        return usuarioService.atualizarFoto(usuarioId, foto, authentication.getName());
    }
}
