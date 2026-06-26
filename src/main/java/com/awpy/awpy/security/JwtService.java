package com.awpy.awpy.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Emite e valida os tokens JWT usados no lugar do HTTP Basic. O token guarda o
 * e-mail (subject) e o papel do principal (claim "role": USUARIO/PARCEIRO/
 * ADMINISTRADOR/FUNCIONARIO) — esses dois dados bastam pro JwtAuthenticationFilter
 * recriar a autenticação sem precisar consultar o banco a cada requisição.
 */
@Component
@Slf4j
public class JwtService {

    private static final String CLAIM_ROLE = "role";

    private final SecretKey secretKey;
    private final long expiracaoEmMinutos;

    public JwtService(
            @Value("${awpy.jwt.secret:#{null}}") String secretConfigurado,
            @Value("${awpy.jwt.expiracao-minutos:120}") long expiracaoEmMinutos) {
        this.expiracaoEmMinutos = expiracaoEmMinutos;

        if (secretConfigurado == null || secretConfigurado.isBlank()) {
            log.warn("awpy.jwt.secret não configurado — usando uma chave gerada só para esta execução " +
                    "(tokens emitidos antes de um restart deixam de ser válidos). Configure essa propriedade " +
                    "antes de qualquer ambiente real.");
            this.secretKey = Jwts.SIG.HS256.key().build();
        } else {
            this.secretKey = Keys.hmacShaKeyFor(secretConfigurado.getBytes(StandardCharsets.UTF_8));
        }
    }

    public String gerarToken(String email, String role) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim(CLAIM_ROLE, role)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusSeconds(expiracaoEmMinutos * 60)))
                .signWith(secretKey)
                .compact();
    }

    /** Retorna vazio (em vez de lançar) para que requisições com token ausente/inválido sigam como anônimas. */
    public Optional<Claims> validar(String token) {
        try {
            return Optional.of(Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public String extrairRole(Claims claims) {
        return claims.get(CLAIM_ROLE, String.class);
    }
}
