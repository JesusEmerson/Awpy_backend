package com.awpy.awpy.service;

import com.awpy.awpy.dto.usuario.UsuarioCadastroRequest;
import com.awpy.awpy.dto.usuario.UsuarioResponse;
import com.awpy.awpy.model.HistoricoPontos;
import com.awpy.awpy.model.Usuario;
import com.awpy.awpy.repository.HistoricoPontosRepository;
import com.awpy.awpy.repository.UsuarioRepository;
import com.awpy.awpy.service.exception.RecursoNaoEncontradoException;
import com.awpy.awpy.service.exception.RegraNegocioException;
import com.awpy.awpy.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final String SUBPASTA_FOTOS = "usuarios";

    private final UsuarioRepository usuarioRepository;
    private final HistoricoPontosRepository historicoPontosRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Transactional
    public UsuarioResponse cadastrar(UsuarioCadastroRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new RegraNegocioException("e-mail já cadastrado");
        }
        if (usuarioRepository.existsByCpfCnpj(request.cpfCnpj())) {
            throw new RegraNegocioException("CPF/CNPJ já cadastrado");
        }

        Usuario usuario = Usuario.builder()
                .nomeCompleto(request.nomeCompleto())
                .cpfCnpj(request.cpfCnpj())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .telefone(request.telefone())
                .endereco(request.endereco())
                .cep(request.cep())
                .saldoPontos(0L)
                .build();

        return UsuarioResponse.fromEntity(usuarioRepository.save(usuario));
    }

    /**
     * Crédito de pontos (ex.: pontuação de uma compra no parceiro, ajuste manual).
     * O PDF não detalha como o usuário ganha pontos, então mantemos essa operação
     * simples e restrita a ADMINISTRADOR/FUNCIONARIO (ver SecurityConfig).
     *
     * Atualiza o saldo trocável por benefícios (Usuario.saldoPontos, nunca reseta) E
     * grava um registro em HistoricoPontos com a data — esse histórico é o que
     * alimenta o ranking mensal, que soma só os pontos ganhos dentro do mês corrente.
     */
    @Transactional
    public UsuarioResponse creditarPontos(Long usuarioId, Long pontos) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("usuário não encontrado"));

        usuario.setSaldoPontos(usuario.getSaldoPontos() + pontos);
        usuarioRepository.save(usuario);

        historicoPontosRepository.save(HistoricoPontos.builder()
                .usuario(usuario)
                .pontos(pontos)
                .dataHora(LocalDateTime.now())
                .build());

        return UsuarioResponse.fromEntity(usuario);
    }

    /**
     * Foto de perfil do usuário é autosserviço (diferente da foto do parceiro, que o
     * PDF coloca sob gestão do admin) — por isso confere a mesma regra de identidade
     * usada nos cupons: o id do path só vale se for o e-mail autenticado.
     */
    @Transactional
    public UsuarioResponse atualizarFoto(Long usuarioId, MultipartFile foto, String emailAutenticado) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("usuário não encontrado"));

        if (!usuario.getEmail().equalsIgnoreCase(emailAutenticado)) {
            throw new AccessDeniedException("usuário só pode alterar a própria foto");
        }

        String fotoUrl = fileStorageService.salvar(foto, SUBPASTA_FOTOS);
        usuario.setFotoUrl(fotoUrl);

        return UsuarioResponse.fromEntity(usuarioRepository.save(usuario));
    }
}
