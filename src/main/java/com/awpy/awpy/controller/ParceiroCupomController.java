package com.awpy.awpy.controller;

import com.awpy.awpy.dto.cupom.CardValidacaoResponse;
import com.awpy.awpy.service.CupomService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parceiros/me/cupons")
@RequiredArgsConstructor
public class ParceiroCupomController {

    private final CupomService cupomService;

    @GetMapping("/{qrCodeUnico}")
    public CardValidacaoResponse validar(@PathVariable String qrCodeUnico, Authentication authentication) {
        return cupomService.buscarParaValidacao(authentication.getName(), qrCodeUnico);
    }

    @PostMapping("/{qrCodeUnico}/confirmar")
    public CardValidacaoResponse confirmar(@PathVariable String qrCodeUnico, Authentication authentication) {
        return cupomService.confirmarUso(authentication.getName(), qrCodeUnico);
    }

    @PostMapping("/{qrCodeUnico}/cancelar")
    public CardValidacaoResponse cancelar(@PathVariable String qrCodeUnico, Authentication authentication) {
        return cupomService.cancelar(authentication.getName(), qrCodeUnico);
    }
}
