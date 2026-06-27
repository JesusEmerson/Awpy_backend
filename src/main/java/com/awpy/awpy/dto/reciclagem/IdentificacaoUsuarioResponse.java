package com.awpy.awpy.dto.reciclagem;

import com.awpy.awpy.model.Usuario;

/** Retorno do scanner do parceiro ao ler o QR Code da Home do usuário (não o do cupom). */
public record IdentificacaoUsuarioResponse(
        Long usuarioId,
        String nome,
        String fotoUrl
) {
    public static IdentificacaoUsuarioResponse fromEntity(Usuario usuario) {
        return new IdentificacaoUsuarioResponse(usuario.getId(), usuario.getNomeCompleto(), usuario.getFotoUrl());
    }
}
