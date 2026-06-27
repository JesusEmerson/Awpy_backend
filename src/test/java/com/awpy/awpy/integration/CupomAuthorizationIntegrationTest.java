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
 * Os endpoints de cupom (usuário e parceiro) derivam a identidade só do token —
 * não existe id de outra pessoa no path pra um cliente malicioso trocar (era assim
 * antes; foi refatorado pra "/me" justamente pra eliminar essa classe de bug por
 * construção). O que ainda precisa ser testado é a regra de pertencimento real:
 * um parceiro não pode confirmar/cancelar/ver um cupom que pertence a OUTRO
 * parceiro, mesmo sabendo o QR Code.
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
    void usuarioVeSomenteOProprioCupomAtivo() throws Exception {
        cadastrarUsuario("a@teste.com");
        cadastrarUsuario("b@teste.com");
        String tokenB = loginUsuario("b@teste.com");

        // B nunca resgatou nada: deve dar 404, nunca ver o cupom de A
        mockMvc.perform(get("/api/usuarios/me/cupons/ativo")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    void parceiroNaoPodeValidarCupomDeOutroParceiro() throws Exception {
        Long idParceiroA = cadastrarParceiroComoAdmin("pa@teste.com");
        cadastrarParceiroComoAdmin("pb@teste.com");
        String tokenAdmin = loginAdmin();
        Long idBeneficio = cadastrarBeneficio(tokenAdmin, idParceiroA);

        cadastrarUsuario("comprador@teste.com");
        creditarPontos(tokenAdmin, "comprador@teste.com", 1000L);
        String tokenComprador = loginUsuario("comprador@teste.com");
        String qrCode = resgatarCupom(tokenComprador, idBeneficio);

        String tokenParceiroB = loginParceiro("pb@teste.com");

        // RegraNegocioException -> 409, igual às outras regras de cupom (já
        // utilizado/expirado) — não é um AccessDeniedException de autorização.
        mockMvc.perform(get("/api/parceiros/me/cupons/{qr}", qrCode)
                        .header("Authorization", "Bearer " + tokenParceiroB))
                .andExpect(status().isConflict());
    }

    @Test
    void parceiroDonoDoCupomConsegueValidarENaoRecebeForbidden() throws Exception {
        Long idParceiroA = cadastrarParceiroComoAdmin("pa2@teste.com");
        String tokenAdmin = loginAdmin();
        Long idBeneficio = cadastrarBeneficio(tokenAdmin, idParceiroA);

        cadastrarUsuario("comprador2@teste.com");
        creditarPontos(tokenAdmin, "comprador2@teste.com", 1000L);
        String tokenComprador = loginUsuario("comprador2@teste.com");
        String qrCode = resgatarCupom(tokenComprador, idBeneficio);

        String tokenParceiroA = loginParceiro("pa2@teste.com");

        mockMvc.perform(get("/api/parceiros/me/cupons/{qr}", qrCode)
                        .header("Authorization", "Bearer " + tokenParceiroA))
                .andExpect(status().isOk());
    }

    @Test
    void requisicaoSemTokenRecebeUnauthorized() throws Exception {
        mockMvc.perform(get("/api/ranking"))
                .andExpect(status().isUnauthorized());
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

        return objectMapper.readTree(body).get("perfil").get("id").asLong();
    }

    private Long cadastrarParceiroComoAdmin(String email) throws Exception {
        String tokenAdmin = loginAdmin();
        String cnpj = String.valueOf(System.nanoTime()).substring(0, 14);

        String body = mockMvc.perform(post("/api/parceiros")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nomeEstabelecimento":"Parceiro Teste",
                                  "cnpj":"%s",
                                  "email":"%s",
                                  "senha":"senha1234",
                                  "telefone":"11999999999",
                                  "endereco":"Rua A, 123"
                                }
                                """.formatted(cnpj, email))
                ).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("id").asLong();
    }

    private Long cadastrarBeneficio(String tokenAdmin, Long parceiroId) throws Exception {
        String body = mockMvc.perform(post("/api/beneficios")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome":"Benefício Teste",
                                  "descricao":"desc",
                                  "custoEmPontos":100,
                                  "parceiroId":%d
                                }
                                """.formatted(parceiroId))
                ).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("id").asLong();
    }

    private void creditarPontos(String tokenAdmin, String emailUsuario, Long pontos) throws Exception {
        Long usuarioId = objectMapper.readTree(
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"%s\",\"senha\":\"senha1234\"}".formatted(emailUsuario)))
                        .andReturn().getResponse().getContentAsString()
        ).get("perfil").get("id").asLong();

        mockMvc.perform(post("/api/usuarios/{id}/pontos", usuarioId)
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pontos\":%d}".formatted(pontos)))
                .andExpect(status().isOk());
    }

    private String resgatarCupom(String tokenUsuario, Long beneficioId) throws Exception {
        String body = mockMvc.perform(post("/api/usuarios/me/cupons")
                        .header("Authorization", "Bearer " + tokenUsuario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"beneficioId\":%d}".formatted(beneficioId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("qrCodeUnico").asText();
    }

    private String loginUsuario(String email) throws Exception {
        return login(email, "senha1234");
    }

    private String loginParceiro(String email) throws Exception {
        return login(email, "senha1234");
    }

    private String loginAdmin() throws Exception {
        return login("admin@awpy.com", "troque-esta-senha");
    }

    private String login(String email, String senha) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha))
                ).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("token").asText();
    }
}
