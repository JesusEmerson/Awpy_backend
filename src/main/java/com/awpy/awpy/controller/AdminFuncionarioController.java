package com.awpy.awpy.controller;

import com.awpy.awpy.dto.admin.AdminFuncionarioCadastroRequest;
import com.awpy.awpy.dto.admin.AdminFuncionarioResponse;
import com.awpy.awpy.service.AdminFuncionarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminFuncionarioController {

    private final AdminFuncionarioService adminFuncionarioService;

    @PostMapping("/cadastro")
    public ResponseEntity<AdminFuncionarioResponse> cadastrar(
            @Valid @RequestBody AdminFuncionarioCadastroRequest request) {
        AdminFuncionarioResponse response = adminFuncionarioService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/me/foto")
    public AdminFuncionarioResponse atualizarFotoPropria(
            @RequestParam("foto") MultipartFile foto, Authentication authentication) {
        return adminFuncionarioService.atualizarFotoPropria(authentication.getName(), foto);
    }
}
