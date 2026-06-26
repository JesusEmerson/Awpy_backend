package com.awpy.awpy.dto.usuario;

import com.awpy.awpy.model.Usuario;

public record UsuarioResponse(
        Long id,
        String nomeCompleto,
        String email,
        Long saldoPontos,
        String fotoUrl
) {
    public static UsuarioResponse fromEntity(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNomeCompleto(),
                usuario.getEmail(),
                usuario.getSaldoPontos(),
                usuario.getFotoUrl()
        );
    }
}
