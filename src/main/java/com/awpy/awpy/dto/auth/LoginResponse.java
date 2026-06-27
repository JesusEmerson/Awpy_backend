package com.awpy.awpy.dto.auth;

/**
 * "role" vem explícito no corpo (não só dentro do JWT) pra quem consome a API não
 * precisar decodificar o token só pra saber pra onde navegar — é exatamente o valor
 * usado na claim "role" do JWT, então os dois sempre concordam.
 */
public record LoginResponse<T>(String token, String role, T perfil) {
}
