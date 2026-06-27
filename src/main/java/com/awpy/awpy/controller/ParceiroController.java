package com.awpy.awpy.controller;

import com.awpy.awpy.dto.parceiro.ParceiroCadastroRequest;
import com.awpy.awpy.dto.parceiro.ParceiroResponse;
import com.awpy.awpy.dto.parceiro.ParceiroResumoResponse;
import com.awpy.awpy.service.ParceiroService;
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

import java.util.List;

@RestController
@RequestMapping("/api/parceiros")
@RequiredArgsConstructor
public class ParceiroController {

    private final ParceiroService parceiroService;

    @PostMapping
    public ResponseEntity<ParceiroResponse> cadastrar(@Valid @RequestBody ParceiroCadastroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(parceiroService.cadastrar(request));
    }

    @PostMapping("/{parceiroId}/foto")
    public ParceiroResponse atualizarFoto(@PathVariable Long parceiroId, @RequestParam("foto") MultipartFile foto) {
        return parceiroService.atualizarFoto(parceiroId, foto);
    }

    @PostMapping("/me/foto")
    public ParceiroResponse atualizarFotoPropria(@RequestParam("foto") MultipartFile foto, Authentication authentication) {
        return parceiroService.atualizarFotoPropria(authentication.getName(), foto);
    }

    @GetMapping
    public List<ParceiroResumoResponse> listar() {
        return parceiroService.listar();
    }
}
