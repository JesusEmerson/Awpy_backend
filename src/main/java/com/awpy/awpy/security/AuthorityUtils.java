package com.awpy.awpy.security;

import org.springframework.security.core.Authentication;

/**
 * Os três logins (usuário/parceiro/admin) compartilham o mesmo AuthenticationManager
 * (ver SecurityConfig) — ele tenta autenticar contra os três UserDetailsService até
 * um aceitar. Isso significa que, em tese, um e-mail/senha que bate por coincidência
 * num cadastro de OUTRO perfil também passaria. Por isso cada controller de login
 * confere, depois de autenticar, que o papel retornado é realmente o esperado para
 * aquele endpoint antes de emitir o token.
 */
public final class AuthorityUtils {

    private AuthorityUtils() {
    }

    public static boolean temPapel(Authentication authentication, String role) {
        String authority = "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .anyMatch(concedida -> concedida.getAuthority().equals(authority));
    }
}
