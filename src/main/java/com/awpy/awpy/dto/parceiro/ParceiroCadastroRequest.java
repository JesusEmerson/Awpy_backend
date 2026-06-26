package com.awpy.awpy.dto.parceiro;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ParceiroCadastroRequest(

        @NotBlank(message = "nome do estabelecimento é obrigatório")
        String nomeEstabelecimento,

        @NotBlank(message = "e-mail é obrigatório")
        @Email(message = "e-mail em formato inválido")
        String email,

        @NotBlank(message = "senha é obrigatória")
        @Size(min = 8, message = "senha deve ter no mínimo 8 caracteres")
        String senha,

        Double percentualDesconto,

        Double percentualCashback
) {
}
