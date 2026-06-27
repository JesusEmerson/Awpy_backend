package com.awpy.awpy.config;

import com.awpy.awpy.model.Beneficio;
import com.awpy.awpy.model.HistoricoPontos;
import com.awpy.awpy.model.Parceiro;
import com.awpy.awpy.model.Usuario;
import com.awpy.awpy.repository.BeneficioRepository;
import com.awpy.awpy.repository.HistoricoPontosRepository;
import com.awpy.awpy.repository.ParceiroRepository;
import com.awpy.awpy.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mesmo problema do AdminBootstrapRunner, mas pro parceiro e pro usuário comum: o
 * H2 é em memória, então qualquer conta criada manualmente (curl, app) some a cada
 * restart. Sem isso, testar as telas de parceiro/usuário exige recriar manualmente
 * conta + parceiro + benefício + crédito de pontos toda vez. As checagens são
 * independentes (cada bloco só semeia se aquela tabela estiver vazia), pra não
 * recriar duplicado se só uma parte já existir.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DevDataSeedRunner implements ApplicationRunner {

    private static final String SENHA_PADRAO = "teste1234";

    private final ParceiroRepository parceiroRepository;
    private final BeneficioRepository beneficioRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistoricoPontosRepository historicoPontosRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${awpy.seed.parceiro.email:parceiro@awpy.com}")
    private String emailParceiro;

    @Value("${awpy.seed.usuario.email:usuario@awpy.com}")
    private String emailUsuario;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Parceiro parceiro = seedParceiro();
        seedBeneficio(parceiro);
        seedUsuario();
    }

    private Parceiro seedParceiro() {
        if (parceiroRepository.count() > 0) {
            return parceiroRepository.findAll().get(0);
        }

        Parceiro parceiro = Parceiro.builder()
                .nomeEstabelecimento("Parceiro de Teste")
                .cnpj("11222333000181")
                .email(emailParceiro)
                .senha(passwordEncoder.encode(SENHA_PADRAO))
                .telefone("11999990000")
                .endereco("Rua de Teste, 100")
                .percentualDesconto(10.0)
                .percentualCashback(5.0)
                .ativo(true)
                .build();

        parceiroRepository.save(parceiro);
        log.warn("Parceiro de teste criado: e-mail '{}', senha '{}'.", emailParceiro, SENHA_PADRAO);
        return parceiro;
    }

    private void seedBeneficio(Parceiro parceiro) {
        if (beneficioRepository.count() > 0) {
            return;
        }

        Beneficio beneficio = Beneficio.builder()
                .nome("Café Grátis (Teste)")
                .descricao("1 café expresso — benefício de teste")
                .custoEmPontos(100L)
                .percentualDesconto(0.0)
                .percentualCashback(0.0)
                .parceiro(parceiro)
                .ativo(true)
                .build();

        beneficioRepository.save(beneficio);
        log.warn("Benefício de teste criado: '{}' (custo: 100 pontos, parceiro: {}).",
                beneficio.getNome(), parceiro.getNomeEstabelecimento());
    }

    private void seedUsuario() {
        if (usuarioRepository.count() > 0) {
            return;
        }

        Usuario usuario = Usuario.builder()
                .nomeCompleto("Usuário de Teste")
                .cpfCnpj("12345678901")
                .email(emailUsuario)
                .senha(passwordEncoder.encode(SENHA_PADRAO))
                .telefone("11988880000")
                .endereco("Rua de Teste, 200")
                .cep("01001000")
                .saldoPontos(500L)
                .qrCodeUsuario(UUID.randomUUID().toString())
                .build();

        usuarioRepository.save(usuario);

        historicoPontosRepository.save(HistoricoPontos.builder()
                .usuario(usuario)
                .pontos(500L)
                .dataHora(LocalDateTime.now())
                .build());

        log.warn("Usuário de teste criado: e-mail '{}', senha '{}', saldo inicial: 500 pontos.",
                emailUsuario, SENHA_PADRAO);
    }
}
