package com.awpy.awpy.service;

import com.awpy.awpy.dto.auth.RecuperarSenhaResponse;
import com.awpy.awpy.model.CodigoRecuperacaoSenha;
import com.awpy.awpy.repository.AdminFuncionarioRepository;
import com.awpy.awpy.repository.CodigoRecuperacaoSenhaRepository;
import com.awpy.awpy.repository.ParceiroRepository;
import com.awpy.awpy.repository.UsuarioRepository;
import com.awpy.awpy.service.exception.RecursoNaoEncontradoException;
import com.awpy.awpy.service.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * MODO DEMO — sem SMTP configurado ainda (ver CodigoRecuperacaoSenha). O código é
 * devolvido na própria resposta da API em vez de ser enviado por e-mail. Antes de
 * qualquer ambiente real, isso precisa virar um envio de e-mail de fato e o campo
 * "codigo" precisa sair da resposta.
 */
@Service
@RequiredArgsConstructor
public class RecuperacaoSenhaService {

    private static final int VALIDADE_EM_MINUTOS = 15;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final ParceiroRepository parceiroRepository;
    private final AdminFuncionarioRepository adminFuncionarioRepository;
    private final CodigoRecuperacaoSenhaRepository codigoRecuperacaoSenhaRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RecuperarSenhaResponse solicitar(String email) {
        boolean existe = usuarioRepository.existsByEmail(email)
                || parceiroRepository.existsByEmail(email)
                || adminFuncionarioRepository.existsByEmail(email);

        if (!existe) {
            throw new RecursoNaoEncontradoException("e-mail não cadastrado");
        }

        String codigo = gerarCodigoDeSeisDigitos();

        codigoRecuperacaoSenhaRepository.save(CodigoRecuperacaoSenha.builder()
                .email(email)
                .codigo(codigo)
                .dataExpiracao(LocalDateTime.now().plusMinutes(VALIDADE_EM_MINUTOS))
                .usado(false)
                .build());

        return new RecuperarSenhaResponse(
                "Código gerado (modo demo — em produção isso seria enviado por e-mail)", codigo);
    }

    @Transactional
    public void redefinir(String email, String codigo, String novaSenha) {
        CodigoRecuperacaoSenha codigoRecuperacao = codigoRecuperacaoSenhaRepository
                .findByEmailAndCodigoAndUsadoFalse(email, codigo)
                .orElseThrow(() -> new RegraNegocioException("código inválido", "CODIGO_INVALIDO"));

        if (codigoRecuperacao.getDataExpiracao().isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("código expirado", "CODIGO_EXPIRADO");
        }

        atualizarSenha(email, novaSenha);

        codigoRecuperacao.setUsado(true);
        codigoRecuperacaoSenhaRepository.save(codigoRecuperacao);
    }

    private void atualizarSenha(String email, String novaSenha) {
        String senhaCodificada = passwordEncoder.encode(novaSenha);

        usuarioRepository.findByEmail(email).ifPresentOrElse(
                usuario -> {
                    usuario.setSenha(senhaCodificada);
                    usuarioRepository.save(usuario);
                },
                () -> parceiroRepository.findByEmail(email).ifPresentOrElse(
                        parceiro -> {
                            parceiro.setSenha(senhaCodificada);
                            parceiroRepository.save(parceiro);
                        },
                        () -> adminFuncionarioRepository.findByEmail(email).ifPresentOrElse(
                                admin -> {
                                    admin.setSenha(senhaCodificada);
                                    adminFuncionarioRepository.save(admin);
                                },
                                () -> {
                                    throw new RecursoNaoEncontradoException("e-mail não cadastrado");
                                }
                        )
                )
        );
    }

    private String gerarCodigoDeSeisDigitos() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
