package com.awpy.awpy.dto.admin;

import com.awpy.awpy.model.enums.NivelPermissao;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminFuncionarioCadastroRequest(

        @NotBlank(message = "nome completo é obrigatório")
        String nomeCompleto,

        @NotBlank(message = "e-mail é obrigatório")
        @Email(message = "e-mail em formato inválido")
        String email,

        @NotBlank(message = "senha é obrigatória")
        @Size(min = 8, message = "senha deve ter no mínimo 8 caracteres")
        String senha,

        @NotNull(message = "nível de permissão é obrigatório")
        NivelPermissao nivelPermissao
) {
}
