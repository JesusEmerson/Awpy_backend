package com.awpy.awpy.controller;

import com.awpy.awpy.dto.cupom.CupomResgateRequest;
import com.awpy.awpy.dto.cupom.CupomResponse;
import com.awpy.awpy.service.CupomService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/cupons")
@RequiredArgsConstructor
public class CupomController {

    private final CupomService cupomService;

    @PostMapping
    public ResponseEntity<CupomResponse> resgatar(
            @PathVariable Long usuarioId,
            @Valid @RequestBody CupomResgateRequest request,
            Authentication authentication) {
        CupomResponse response = cupomService.resgatar(usuarioId, request.beneficioId(), authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/ativo")
    public ResponseEntity<CupomResponse> buscarAtivo(@PathVariable Long usuarioId, Authentication authentication) {
        return ResponseEntity.ok(cupomService.buscarCupomAtivo(usuarioId, authentication.getName()));
    }
}
