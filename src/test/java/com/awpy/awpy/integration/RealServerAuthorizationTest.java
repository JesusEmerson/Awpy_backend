package com.awpy.awpy.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Existe separado de CupomAuthorizationIntegrationTest porque MockMvc (usado lá) NÃO
 * simula o mecanismo de "error dispatch" do servlet container — então um bug em que
 * authenticationEntryPoint/accessDeniedHandler chamavam response.sendError(...) (o que
 * dispara um forward interno para /error, reprocessado pela cadeia de filtros, onde o
 * JwtAuthenticationFilter não reautentica por padrão) passava 100% verde no MockMvc e
 * só aparecia como 401-no-lugar-de-403 num servidor real. Este teste sobe um Tomcat
 * embarcado de verdade (RANDOM_PORT) pra travar essa classe de regressão.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@DirtiesContext
class RealServerAuthorizationTest {

    @org.springframework.beans.factory.annotation.Autowired
    private TestRestTemplate restTemplate;

    @Test
    void papelErradoRetorna403ENao401NoServidorReal() {
        cadastrarUsuario("real-server-a@teste.com");
        String token = loginUsuario("real-server-a@teste.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        // ROLE_USUARIO tentando acessar endpoint exclusivo de PARCEIRO
        var response = restTemplate.exchange(
                "/api/parceiros/1/cupons/qualquer-qr",
                org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        org.assertj.core.api.Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void semTokenRetorna401NoServidorReal() {
        var response = restTemplate.getForEntity("/api/ranking", String.class);

        org.assertj.core.api.Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private void cadastrarUsuario(String email) {
        String cpf = String.valueOf(System.nanoTime()).substring(0, 11);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = """
                {
                  "nomeCompleto":"Usuario Teste",
                  "cpfCnpj":"%s",
                  "email":"%s",
                  "senha":"senha1234",
                  "telefone":"11999999999",
                  "endereco":"Rua A, 123",
                  "cep":"12345678"
                }
                """.formatted(cpf, email);

        var response = restTemplate.postForEntity("/api/usuarios/cadastro", new HttpEntity<>(body, headers), String.class);
        org.assertj.core.api.Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private String loginUsuario(String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"email\":\"%s\",\"senha\":\"senha1234\"}".formatted(email);

        var response = restTemplate.postForEntity("/api/usuarios/login", new HttpEntity<>(body, headers), String.class);
        org.assertj.core.api.Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        String json = response.getBody();
        return json.substring(json.indexOf("\"token\":\"") + 9, json.indexOf("\"", json.indexOf("\"token\":\"") + 9));
    }
}
