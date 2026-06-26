package com.awpy.awpy.dto.auth;

public record LoginResponse<T>(String token, T perfil) {
}
