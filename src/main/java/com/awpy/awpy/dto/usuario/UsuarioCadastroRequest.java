package com.awpy.awpy.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioCadastroRequest(

        @NotBlank(message = "nome completo é obrigatório")
        String nomeCompleto,

        @NotBlank(message = "CPF/CNPJ é obrigatório")
        @Pattern(regexp = "\\d{11}|\\d{14}", message = "CPF/CNPJ deve conter 11 (CPF) ou 14 (CNPJ) dígitos numéricos")
        String cpfCnpj,

        @NotBlank(message = "e-mail é obrigatório")
        @Email(message = "e-mail em formato inválido")
        String email,

        @NotBlank(message = "senha é obrigatória")
        @Size(min = 8, message = "senha deve ter no mínimo 8 caracteres")
        String senha,

        @NotBlank(message = "telefone é obrigatório")
        String telefone,

        @NotBlank(message = "endereço é obrigatório")
        String endereco,

        @NotBlank(message = "CEP é obrigatório")
        @Pattern(regexp = "\\d{8}", message = "CEP deve conter 8 dígitos numéricos")
        String cep
) {
}
