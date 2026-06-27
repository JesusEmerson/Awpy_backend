package com.awpy.awpy.controller;

import com.awpy.awpy.dto.reciclagem.IdentificacaoUsuarioResponse;
import com.awpy.awpy.dto.reciclagem.ReciclagemRequest;
import com.awpy.awpy.dto.reciclagem.ReciclagemResponse;
import com.awpy.awpy.service.ReciclagemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parceiros/me")
@RequiredArgsConstructor
public class ReciclagemController {

    private final ReciclagemService reciclagemService;

    @GetMapping("/usuarios/{qrCodeUsuario}")
    public IdentificacaoUsuarioResponse identificarUsuario(@PathVariable String qrCodeUsuario) {
        return reciclagemService.identificarPorQrCode(qrCodeUsuario);
    }

    @PostMapping("/reciclagens")
    public ReciclagemResponse registrar(@Valid @RequestBody ReciclagemRequest request, Authentication authentication) {
        return reciclagemService.registrar(
                authentication.getName(), request.usuarioId(), request.materialId(), request.quilos());
    }
}
