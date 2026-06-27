package com.awpy.awpy.service;

import com.awpy.awpy.dto.cupom.CardValidacaoResponse;
import com.awpy.awpy.dto.cupom.CupomResponse;
import com.awpy.awpy.model.Beneficio;
import com.awpy.awpy.model.Cupom;
import com.awpy.awpy.model.Parceiro;
import com.awpy.awpy.model.Usuario;
import com.awpy.awpy.model.enums.StatusCupom;
import com.awpy.awpy.repository.BeneficioRepository;
import com.awpy.awpy.repository.CupomRepository;
import com.awpy.awpy.repository.ParceiroRepository;
import com.awpy.awpy.repository.UsuarioRepository;
import com.awpy.awpy.service.exception.RecursoNaoEncontradoException;
import com.awpy.awpy.service.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Todas as operações aqui derivam o usuário/parceiro do e-mail autenticado (token),
 * nunca de um id vindo do cliente — não existe rota "/usuarios/{id}/cupons", só
 * "/usuarios/me/cupons" (ver UsuarioController/ParceiroCupomController). Isso evita
 * IDOR por construção e também poupa o app de ter que guardar o próprio id numérico
 * só para montar URLs.
 */
@Service
@RequiredArgsConstructor
public class CupomService {

    private static final int VALIDADE_EM_DIAS = 30;
    private static final int LIMITE_CUPONS_ATIVOS = 3;

    private final UsuarioRepository usuarioRepository;
    private final BeneficioRepository beneficioRepository;
    private final CupomRepository cupomRepository;
    private final ParceiroRepository parceiroRepository;

    @Transactional
    public CupomResponse resgatar(String emailAutenticado, Long beneficioId) {
        Usuario usuario = carregarUsuarioPorEmail(emailAutenticado);

        Beneficio beneficio = beneficioRepository.findById(beneficioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("benefício não encontrado"));

        if (!beneficio.getAtivo()) {
            throw new RegraNegocioException("benefício indisponível", "BENEFICIO_INDISPONIVEL");
        }

        if (usuario.getSaldoPontos() < beneficio.getCustoEmPontos()) {
            throw new RegraNegocioException("saldo de pontos insuficiente", "SALDO_INSUFICIENTE");
        }

        if (cupomRepository.countByUsuarioAndStatus(usuario, StatusCupom.ATIVO) >= LIMITE_CUPONS_ATIVOS) {
            throw new RegraNegocioException(
                    "usuário já atingiu o limite de " + LIMITE_CUPONS_ATIVOS + " cupons ativos",
                    "LIMITE_CUPONS_ATIVOS_ATINGIDO");
        }

        LocalDateTime agora = LocalDateTime.now();
        Cupom cupom = Cupom.builder()
                .usuario(usuario)
                .beneficio(beneficio)
                .parceiro(beneficio.getParceiro())
                .qrCodeUnico(UUID.randomUUID().toString())
                .status(StatusCupom.ATIVO)
                .dataGeracao(agora)
                .dataExpiracao(agora.plusDays(VALIDADE_EM_DIAS))
                .build();

        return CupomResponse.fromEntity(cupomRepository.save(cupom));
    }

    /** Até LIMITE_CUPONS_ATIVOS itens — lista vazia se não houver nenhum (nunca 404). */
    @Transactional
    public List<CupomResponse> buscarCuponsAtivos(String emailAutenticado) {
        Usuario usuario = carregarUsuarioPorEmail(emailAutenticado);

        List<Cupom> cupons = cupomRepository.findByUsuarioAndStatus(usuario, StatusCupom.ATIVO);
        cupons.forEach(this::expirarSeNecessario);

        return cupons.stream()
                .filter(cupom -> cupom.getStatus() == StatusCupom.ATIVO)
                .map(CupomResponse::fromEntity)
                .toList();
    }

    /** Histórico completo (ativo + utilizados + expirados), mais recente primeiro. */
    @Transactional
    public List<CupomResponse> listarTodos(String emailAutenticado) {
        Usuario usuario = carregarUsuarioPorEmail(emailAutenticado);

        List<Cupom> cupons = cupomRepository.findByUsuarioOrderByDataGeracaoDesc(usuario);
        cupons.forEach(this::expirarSeNecessario);

        return cupons.stream().map(CupomResponse::fromEntity).toList();
    }

    @Transactional
    public CardValidacaoResponse buscarParaValidacao(String emailAutenticado, String qrCodeUnico) {
        return CardValidacaoResponse.fromEntity(buscarCupomValidoDoParceiro(emailAutenticado, qrCodeUnico));
    }

    @Transactional
    public CardValidacaoResponse confirmarUso(String emailAutenticado, String qrCodeUnico) {
        Cupom cupom = buscarCupomValidoDoParceiro(emailAutenticado, qrCodeUnico);

        Usuario usuario = cupom.getUsuario();
        usuario.setSaldoPontos(usuario.getSaldoPontos() - cupom.getBeneficio().getCustoEmPontos());
        usuarioRepository.save(usuario);

        cupom.setStatus(StatusCupom.UTILIZADO);
        cupom.setDataUtilizacao(LocalDateTime.now());
        cupomRepository.save(cupom);

        return CardValidacaoResponse.fromEntity(cupom);
    }

    @Transactional
    public CardValidacaoResponse cancelar(String emailAutenticado, String qrCodeUnico) {
        // Cancelar não altera o cupom: ele permanece ATIVO (se ainda válido) e nenhum
        // ponto é baixado. A validação abaixo apenas garante que o parceiro tem
        // permissão para "ver" esse cupom antes de descartar a leitura.
        return CardValidacaoResponse.fromEntity(buscarCupomValidoDoParceiro(emailAutenticado, qrCodeUnico));
    }

    private Usuario carregarUsuarioPorEmail(String emailAutenticado) {
        return usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new RecursoNaoEncontradoException("usuário não encontrado"));
    }

    private Parceiro carregarParceiroPorEmail(String emailAutenticado) {
        return parceiroRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new RecursoNaoEncontradoException("parceiro não encontrado"));
    }

    private Cupom buscarCupomValidoDoParceiro(String emailAutenticado, String qrCodeUnico) {
        Parceiro parceiro = carregarParceiroPorEmail(emailAutenticado);

        Cupom cupom = cupomRepository.findByQrCodeUnico(qrCodeUnico)
                .orElseThrow(() -> new RecursoNaoEncontradoException("cupom inexistente"));

        expirarSeNecessario(cupom);

        if (!cupom.getParceiro().getId().equals(parceiro.getId())) {
            throw new RegraNegocioException("cupom não pertence ao parceiro logado", "CUPOM_DE_OUTRO_PARCEIRO");
        }
        if (cupom.getStatus() == StatusCupom.UTILIZADO) {
            throw new RegraNegocioException("cupom já utilizado", "CUPOM_JA_UTILIZADO");
        }
        if (cupom.getStatus() == StatusCupom.EXPIRADO) {
            throw new RegraNegocioException("cupom expirado", "CUPOM_EXPIRADO");
        }

        return cupom;
    }

    private void expirarSeNecessario(Cupom cupom) {
        if (cupom.getStatus() == StatusCupom.ATIVO && cupom.getDataExpiracao().isBefore(LocalDateTime.now())) {
            cupom.setStatus(StatusCupom.EXPIRADO);
            cupomRepository.save(cupom);
        }
    }
}
