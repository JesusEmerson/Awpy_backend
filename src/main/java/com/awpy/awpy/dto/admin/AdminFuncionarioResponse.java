package com.awpy.awpy.dto.admin;

import com.awpy.awpy.model.AdminFuncionario;
import com.awpy.awpy.model.enums.NivelPermissao;

public record AdminFuncionarioResponse(
        Long id,
        String nomeCompleto,
        String email,
        NivelPermissao nivelPermissao
) {
    public static AdminFuncionarioResponse fromEntity(AdminFuncionario admin) {
        return new AdminFuncionarioResponse(
                admin.getId(), admin.getNomeCompleto(), admin.getEmail(), admin.getNivelPermissao());
    }
}
