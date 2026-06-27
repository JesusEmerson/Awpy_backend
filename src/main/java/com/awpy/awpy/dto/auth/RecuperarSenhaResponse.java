package com.awpy.awpy.dto.auth;

/**
 * "codigo" só existe aqui porque ainda não há envio de e-mail real configurado
 * (modo demo). Quando o SMTP for configurado, esse campo deve ser removido e a
 * resposta deve ser só a mensagem, com o código indo de fato por e-mail.
 */
public record RecuperarSenhaResponse(String mensagem, String codigo) {
}
