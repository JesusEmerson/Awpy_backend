package com.awpy.awpy.config;

import com.awpy.awpy.security.JwtAuthenticationFilter;
import com.awpy.awpy.service.AdminFuncionarioDetailsService;
import com.awpy.awpy.service.ParceiroDetailsService;
import com.awpy.awpy.service.UsuarioDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Usuário comum, parceiro e administrador/funcionário têm tabelas e credenciais
 * totalmente separadas (conforme regra de negócio), por isso cada um tem seu próprio
 * UserDetailsService e um DaoAuthenticationProvider dedicado. O login é único
 * (POST /api/auth/login, ver AuthController) e usa esse mesmo AuthenticationManager:
 * ele tenta autenticar contra os três providers, nessa ordem, até um aceitar — o
 * app cliente não precisa saber de antemão "que tipo de conta" é aquele e-mail.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;
    private final ParceiroDetailsService parceiroDetailsService;
    private final AdminFuncionarioDetailsService adminFuncionarioDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider usuarioProvider = new DaoAuthenticationProvider(usuarioDetailsService);
        usuarioProvider.setPasswordEncoder(passwordEncoder);

        DaoAuthenticationProvider parceiroProvider = new DaoAuthenticationProvider(parceiroDetailsService);
        parceiroProvider.setPasswordEncoder(passwordEncoder);

        DaoAuthenticationProvider adminProvider = new DaoAuthenticationProvider(adminFuncionarioDetailsService);
        adminProvider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(usuarioProvider, parceiroProvider, adminProvider);
    }

    /**
     * Login é único pros três perfis (POST /api/auth/login — ver AuthController) e
     * devolve um token JWT. Daí em diante, toda requisição autenticada manda esse
     * token em "Authorization: Bearer <token>" — o JwtAuthenticationFilter lê e
     * valida antes de qualquer regra de autorização.
     *
     * Cadastro de administrador/funcionário é restrito a ADMINISTRADOR. Cadastro de
     * parceiro e gestão de benefícios podem ser feitos por ADMINISTRADOR ou FUNCIONARIO
     * (o PDF prevê que funcionário pode ter menos acesso, mas essas duas ações são
     * operação comum do dia a dia administrativo).
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // response.setStatus (não sendError!) — sendError dispara um forward
                // interno para /error (dispatcher ERROR), que passa pela cadeia de
                // filtros de novo; como o JwtAuthenticationFilter (OncePerRequestFilter)
                // ignora esse dispatch por padrão, a requisição chegava "deslogada" na
                // segunda passada e o 403 virava 401.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(
                                (request, response, authException) -> response.setStatus(HttpServletResponse.SC_UNAUTHORIZED))
                        .accessDeniedHandler(
                                (request, response, accessDeniedException) -> response.setStatus(HttpServletResponse.SC_FORBIDDEN)))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // Console H2 só para uso em desenvolvimento (inspecionar o banco em memória).
                // Remover esse permitAll/frameOptions antes de ir para um ambiente real.
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers(
                                "/api/auth/login", "/api/auth/recuperar-senha", "/api/auth/redefinir-senha",
                                "/api/usuarios/cadastro")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/admins/cadastro").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/admins/me/foto").hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.GET, "/api/admin/dashboard").hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.GET, "/api/parceiros").hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.POST, "/api/parceiros").hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")
                        // a regra de /me/foto precisa vir ANTES da regra genérica de
                        // /{id}/foto — Spring Security usa a primeira que casar, e
                        // "*" também casaria com o literal "me".
                        .requestMatchers(HttpMethod.POST, "/api/parceiros/me/foto").hasRole("PARCEIRO")
                        .requestMatchers(HttpMethod.POST, "/api/parceiros/*/foto")
                        .hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.GET, "/api/beneficios/todos").hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.POST, "/api/beneficios").hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.PUT, "/api/beneficios/*").hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.GET, "/api/materiais/todos").hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.POST, "/api/materiais").hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.PUT, "/api/materiais/*").hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.POST, "/api/usuarios/*/pontos")
                        .hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.POST, "/api/usuarios/*/foto").hasRole("USUARIO")
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/me/home").hasRole("USUARIO")
                        .requestMatchers("/api/usuarios/*/cupons/**").hasRole("USUARIO")
                        .requestMatchers("/api/parceiros/me/usuarios/**").hasRole("PARCEIRO")
                        .requestMatchers(HttpMethod.POST, "/api/parceiros/me/reciclagens").hasRole("PARCEIRO")
                        .requestMatchers("/api/parceiros/*/cupons/**").hasRole("PARCEIRO")
                        .requestMatchers("/api/ranking").hasRole("USUARIO")
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
