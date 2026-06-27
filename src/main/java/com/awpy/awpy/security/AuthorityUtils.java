package com.awpy.awpy.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * O login é único (POST /api/auth/login): o AuthenticationManager tenta autenticar
 * contra usuário, parceiro e admin/funcionário, nessa ordem, até um aceitar (ver
 * SecurityConfig). Essa classe extrai qual papel realmente autenticou, pra
 * AuthController saber qual repositório consultar e qual claim "role" colocar no JWT.
 */
public final class AuthorityUtils {

    private AuthorityUtils() {
    }

    public static String extrairPapel(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replaceFirst("^ROLE_", ""))
                .orElseThrow(() -> new IllegalStateException("autenticação sem papel definido"));
    }
}
