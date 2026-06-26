package com.awpy.awpy.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Reproduz, em nível de HTTP real (controller + security + service), o cenário de
 * IDOR encontrado na revisão: um usuário/parceiro autenticado não pode operar
 * recursos de outro usuário/parceiro só porque sabe o id na URL. Esse teste existe
 * para travar essa regressão especificamente — é o bug mais sério já encontrado
 * no projeto.
 *
 * Os ids são capturados das respostas de cadastro (em vez de fixos em "1") porque
 * o gerador IDENTITY do H2 não é revertido pelo rollback do @Transactional entre
 * métodos de teste — cada teste pode receber ids diferentes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CupomAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void usuarioNaoPodeVerCupomAtivoDeOutroUsuario() throws Exception {
        Long idA = cadastrarUsuario("a@teste.com");
        cadastrarUsuario("b@teste.com");
        String tokenB = loginUsuario("b@teste.com");

        mockMvc.perform(get("/api/usuarios/{id}/cupons/ativo", idA)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarioNaoPodeResgatarCupomEmNomeDeOutroUsuario() throws Exception {
        Long idA = cadastrarUsuario("a@teste.com");
        cadastrarUsuario("b@teste.com");
        String tokenB = loginUsuario("b@teste.com");

        mockMvc.perform(post("/api/usuarios/{id}/cupons", idA)
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"beneficioId\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarioAcessandoOProprioRecursoNaoRecebeForbidden() throws Exception {
        Long idA = cadastrarUsuario("a@teste.com");
        String tokenA = loginUsuario("a@teste.com");

        // sem cupom ativo cadastrado: deve dar 404 (recurso não encontrado), nunca 403
        mockMvc.perform(get("/api/usuarios/{id}/cupons/ativo", idA)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    @Test
    void requisicaoSemTokenRecebeUnauthorized() throws Exception {
        mockMvc.perform(get("/api/ranking"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void parceiroNaoPodeValidarCupomUsandoIdDeOutroParceiroAutenticadoComoEle() throws Exception {
        Long idParceiroA = cadastrarParceiroComoAdmin("pa@teste.com");
        cadastrarParceiroComoAdmin("pb@teste.com");
        String tokenParceiroB = loginParceiro("pb@teste.com");

        // pb se autentica, mas tenta usar o id que pertence ao parceiro pa
        mockMvc.perform(get("/api/parceiros/{id}/cupons/qualquer-qr", idParceiroA)
                        .header("Authorization", "Bearer " + tokenParceiroB))
                .andExpect(status().isForbidden());
    }

    @Test
    void parceiroAcessandoOProprioRecursoNaoRecebeForbidden() throws Exception {
        Long idParceiroA = cadastrarParceiroComoAdmin("pa@teste.com");
        String tokenParceiroA = loginParceiro("pa@teste.com");

        // QR inexistente: deve dar 404, nunca 403
        mockMvc.perform(get("/api/parceiros/{id}/cupons/qr-inexistente", idParceiroA)
                        .header("Authorization", "Bearer " + tokenParceiroA))
                .andExpect(status().isNotFound());
    }

    private Long cadastrarUsuario(String email) throws Exception {
        String cpf = String.valueOf(System.nanoTime()).substring(0, 11);
        String body = mockMvc.perform(post("/api/usuarios/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nomeCompleto":"Usuario Teste",
                                  "cpfCnpj":"%s",
                                  "email":"%s",
                                  "senha":"senha1234",
                                  "telefone":"11999999999",
                                  "endereco":"Rua A, 123",
                                  "cep":"12345678"
                                }
                                """.formatted(cpf, email))
                ).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("id").asLong();
    }

    private Long cadastrarParceiroComoAdmin(String email) throws Exception {
        String tokenAdmin = loginAdmin();

        String body = mockMvc.perform(post("/api/parceiros")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nomeEstabelecimento":"Parceiro Teste",
                                  "email":"%s",
                                  "senha":"senha1234"
                                }
                                """.formatted(email))
                ).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("id").asLong();
    }

    private String loginUsuario(String email) throws Exception {
        return login("/api/usuarios/login", email, "senha1234");
    }

    private String loginParceiro(String email) throws Exception {
        return login("/api/parceiros/login", email, "senha1234");
    }

    private String loginAdmin() throws Exception {
        return login("/api/admins/login", "admin@awpy.com", "troque-esta-senha");
    }

    private String login(String url, String email, String senha) throws Exception {
        String body = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha))
                ).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("token").asText();
    }
}
