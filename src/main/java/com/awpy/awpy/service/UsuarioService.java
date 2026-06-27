package com.awpy.awpy.service;

import com.awpy.awpy.dto.auth.LoginResponse;
import com.awpy.awpy.dto.usuario.UsuarioCadastroRequest;
import com.awpy.awpy.dto.usuario.UsuarioHomeResponse;
import com.awpy.awpy.dto.usuario.UsuarioResponse;
import com.awpy.awpy.model.HistoricoPontos;
import com.awpy.awpy.model.Usuario;
import com.awpy.awpy.repository.HistoricoPontosRepository;
import com.awpy.awpy.repository.ReciclagemRepository;
import com.awpy.awpy.repository.UsuarioRepository;
import com.awpy.awpy.security.JwtService;
import com.awpy.awpy.service.exception.RecursoNaoEncontradoException;
import com.awpy.awpy.service.exception.RegraNegocioException;
import com.awpy.awpy.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final String SUBPASTA_FOTOS = "usuarios";
    private static final String ROLE = "USUARIO";

    private final UsuarioRepository usuarioRepository;
    private final HistoricoPontosRepository historicoPontosRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final RankingService rankingService;
    private final ReciclagemRepository reciclagemRepository;
    private final JwtService jwtService;

    /**
     * Devolve token junto (auto-login): o app não precisa fazer uma segunda chamada
     * de login imediatamente depois do cadastro só pra conseguir um token.
     */
    @Transactional
    public LoginResponse<UsuarioResponse> cadastrar(UsuarioCadastroRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new RegraNegocioException("e-mail já cadastrado", "EMAIL_JA_CADASTRADO");
        }
        if (usuarioRepository.existsByCpfCnpj(request.cpfCnpj())) {
            throw new RegraNegocioException("CPF/CNPJ já cadastrado", "CPF_CNPJ_JA_CADASTRADO");
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
                .qrCodeUsuario(UUID.randomUUID().toString())
                .build();

        UsuarioResponse response = UsuarioResponse.fromEntity(usuarioRepository.save(usuario));
        String token = jwtService.gerarToken(request.email(), ROLE);
        return new LoginResponse<>(token, ROLE, response);
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
     * PDF coloca sob gestão do admin) — identidade vem só do e-mail autenticado,
     * sem id no path ("/usuarios/me/foto").
     */
    @Transactional
    public UsuarioResponse atualizarFoto(String emailAutenticado, MultipartFile foto) {
        Usuario usuario = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new RecursoNaoEncontradoException("usuário não encontrado"));

        String fotoUrl = fileStorageService.salvar(foto, SUBPASTA_FOTOS);
        usuario.setFotoUrl(fotoUrl);

        return UsuarioResponse.fromEntity(usuarioRepository.save(usuario));
    }

    /**
     * Agregado pra Home do app (uma chamada só, em vez de o app mobile bater em
     * 2-3 endpoints separados): dados do usuário + ranking mensal top 5. Identidade
     * vem só do token (sem id no path), então não existe IDOR possível aqui — é
     * sempre "meus" dados.
     */
    public UsuarioHomeResponse obterHome(String emailAutenticado) {
        Usuario usuario = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new RecursoNaoEncontradoException("usuário não encontrado"));

        Double totalKgReciclado = reciclagemRepository.somarQuilosPorUsuario(usuario);
        Integer minhaPosicaoRanking = rankingService.minhaPosicao(usuario.getId());
        return UsuarioHomeResponse.montar(usuario, totalKgReciclado, minhaPosicaoRanking, rankingService.topCinco());
    }
}
