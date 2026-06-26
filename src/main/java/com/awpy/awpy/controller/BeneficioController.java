package com.awpy.awpy.controller;

import com.awpy.awpy.dto.beneficio.BeneficioCadastroRequest;
import com.awpy.awpy.dto.beneficio.BeneficioResponse;
import com.awpy.awpy.dto.beneficio.BeneficioUpdateRequest;
import com.awpy.awpy.service.BeneficioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/beneficios")
@RequiredArgsConstructor
public class BeneficioController {

    private final BeneficioService beneficioService;

    @GetMapping
    public List<BeneficioResponse> listar() {
        return beneficioService.listarAtivos();
    }

    @PostMapping
    public ResponseEntity<BeneficioResponse> cadastrar(@Valid @RequestBody BeneficioCadastroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(beneficioService.cadastrar(request));
    }

    @PutMapping("/{beneficioId}")
    public BeneficioResponse editar(
            @PathVariable Long beneficioId, @Valid @RequestBody BeneficioUpdateRequest request) {
        return beneficioService.editar(beneficioId, request);
    }
}
