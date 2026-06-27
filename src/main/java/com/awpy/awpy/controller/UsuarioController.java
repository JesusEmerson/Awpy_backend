package com.awpy.awpy.controller;

import com.awpy.awpy.dto.auth.LoginResponse;
import com.awpy.awpy.dto.usuario.CreditoPontosRequest;
import com.awpy.awpy.dto.usuario.UsuarioCadastroRequest;
import com.awpy.awpy.dto.usuario.UsuarioHomeResponse;
import com.awpy.awpy.dto.usuario.UsuarioResponse;
import com.awpy.awpy.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
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

    private final UsuarioService usuarioService;

    @PostMapping("/cadastro")
    public ResponseEntity<LoginResponse<UsuarioResponse>> cadastrar(
            @Valid @RequestBody UsuarioCadastroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.cadastrar(request));
    }

    @PostMapping("/{usuarioId}/pontos")
    public UsuarioResponse creditarPontos(
            @PathVariable Long usuarioId, @Valid @RequestBody CreditoPontosRequest request) {
        return usuarioService.creditarPontos(usuarioId, request.pontos());
    }

    @PostMapping("/me/foto")
    public UsuarioResponse atualizarFoto(@RequestParam("foto") MultipartFile foto, Authentication authentication) {
        return usuarioService.atualizarFoto(authentication.getName(), foto);
    }

    @GetMapping("/me/home")
    public UsuarioHomeResponse home(Authentication authentication) {
        return usuarioService.obterHome(authentication.getName());
    }
}
