package com.awpy.awpy.controller;

import com.awpy.awpy.dto.material.MaterialCadastroRequest;
import com.awpy.awpy.dto.material.MaterialResponse;
import com.awpy.awpy.dto.material.MaterialUpdateRequest;
import com.awpy.awpy.service.MaterialService;
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
@RequestMapping("/api/materiais")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @GetMapping
    public List<MaterialResponse> listar() {
        return materialService.listarAtivos();
    }

    @GetMapping("/todos")
    public List<MaterialResponse> listarTodos() {
        return materialService.listarTodos();
    }

    @PostMapping
    public ResponseEntity<MaterialResponse> cadastrar(@Valid @RequestBody MaterialCadastroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(materialService.cadastrar(request));
    }

    @PutMapping("/{materialId}")
    public MaterialResponse editar(@PathVariable Long materialId, @Valid @RequestBody MaterialUpdateRequest request) {
        return materialService.editar(materialId, request);
    }
}
