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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios/me/cupons")
@RequiredArgsConstructor
public class CupomController {

    private final CupomService cupomService;

    @PostMapping
    public ResponseEntity<CupomResponse> resgatar(
            @Valid @RequestBody CupomResgateRequest request, Authentication authentication) {
        CupomResponse response = cupomService.resgatar(authentication.getName(), request.beneficioId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/ativos")
    public List<CupomResponse> buscarAtivos(Authentication authentication) {
        return cupomService.buscarCuponsAtivos(authentication.getName());
    }

    @GetMapping
    public List<CupomResponse> listarTodos(Authentication authentication) {
        return cupomService.listarTodos(authentication.getName());
    }
}
