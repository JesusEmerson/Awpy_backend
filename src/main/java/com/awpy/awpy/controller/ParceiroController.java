package com.awpy.awpy.controller;

import com.awpy.awpy.dto.auth.LoginResponse;
import com.awpy.awpy.dto.parceiro.ParceiroCadastroRequest;
import com.awpy.awpy.dto.parceiro.ParceiroLoginRequest;
import com.awpy.awpy.dto.parceiro.ParceiroResponse;
import com.awpy.awpy.repository.ParceiroRepository;
import com.awpy.awpy.security.AuthorityUtils;
import com.awpy.awpy.security.JwtService;
import com.awpy.awpy.service.ParceiroService;
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
@RequestMapping("/api/parceiros")
@RequiredArgsConstructor
public class ParceiroController {

    private static final String ROLE = "PARCEIRO";

    private final ParceiroService parceiroService;
    private final AuthenticationManager authenticationManager;
    private final ParceiroRepository parceiroRepository;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<ParceiroResponse> cadastrar(@Valid @RequestBody ParceiroCadastroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(parceiroService.cadastrar(request));
    }

    @PostMapping("/login")
    public LoginResponse<ParceiroResponse> login(@Valid @RequestBody ParceiroLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

        if (!AuthorityUtils.temPapel(authentication, ROLE)) {
            throw new BadCredentialsException("credenciais inválidas para parceiro");
        }

        ParceiroResponse parceiro = parceiroRepository.findByEmail(request.email())
                .map(ParceiroResponse::fromEntity)
                .orElseThrow();

        String token = jwtService.gerarToken(request.email(), ROLE);
        return new LoginResponse<>(token, parceiro);
    }

    @PostMapping("/{parceiroId}/foto")
    public ParceiroResponse atualizarFoto(@PathVariable Long parceiroId, @RequestParam("foto") MultipartFile foto) {
        return parceiroService.atualizarFoto(parceiroId, foto);
    }
}
