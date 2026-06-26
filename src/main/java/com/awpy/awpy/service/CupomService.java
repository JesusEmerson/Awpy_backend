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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CupomService {

    private static final int VALIDADE_EM_DIAS = 30;

    private final UsuarioRepository usuarioRepository;
    private final BeneficioRepository beneficioRepository;
    private final CupomRepository cupomRepository;
    private final ParceiroRepository parceiroRepository;

    @Transactional
    public CupomResponse resgatar(Long usuarioId, Long beneficioId, String emailAutenticado) {
        Usuario usuario = carregarUsuarioAutenticado(usuarioId, emailAutenticado);

        Beneficio beneficio = beneficioRepository.findById(beneficioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("benefício não encontrado"));

        if (!beneficio.getAtivo()) {
            throw new RegraNegocioException("benefício indisponível");
        }

        if (usuario.getSaldoPontos() < beneficio.getCustoEmPontos()) {
            throw new RegraNegocioException("saldo de pontos insuficiente");
        }

        if (cupomRepository.existsByUsuarioAndStatus(usuario, StatusCupom.ATIVO)) {
            throw new RegraNegocioException("usuário já possui um cupom ativo");
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

    public CupomResponse buscarCupomAtivo(Long usuarioId, String emailAutenticado) {
        Usuario usuario = carregarUsuarioAutenticado(usuarioId, emailAutenticado);

        Cupom cupom = cupomRepository.findByUsuarioAndStatus(usuario, StatusCupom.ATIVO)
                .orElseThrow(() -> new RecursoNaoEncontradoException("usuário não possui cupom ativo"));

        expirarSeNecessario(cupom);
        return CupomResponse.fromEntity(cupom);
    }

    @Transactional
    public CardValidacaoResponse buscarParaValidacao(Long parceiroId, String qrCodeUnico, String emailAutenticado) {
        return CardValidacaoResponse.fromEntity(
                buscarCupomValidoDoParceiro(parceiroId, qrCodeUnico, emailAutenticado));
    }

    @Transactional
    public CardValidacaoResponse confirmarUso(Long parceiroId, String qrCodeUnico, String emailAutenticado) {
        Cupom cupom = buscarCupomValidoDoParceiro(parceiroId, qrCodeUnico, emailAutenticado);

        Usuario usuario = cupom.getUsuario();
        usuario.setSaldoPontos(usuario.getSaldoPontos() - cupom.getBeneficio().getCustoEmPontos());
        usuarioRepository.save(usuario);

        cupom.setStatus(StatusCupom.UTILIZADO);
        cupom.setDataUtilizacao(LocalDateTime.now());
        cupomRepository.save(cupom);

        return CardValidacaoResponse.fromEntity(cupom);
    }

    @Transactional
    public CardValidacaoResponse cancelar(Long parceiroId, String qrCodeUnico, String emailAutenticado) {
        // Cancelar não altera o cupom: ele permanece ATIVO (se ainda válido) e nenhum
        // ponto é baixado. A validação abaixo apenas garante que o parceiro tem
        // permissão para "ver" esse cupom antes de descartar a leitura.
        return CardValidacaoResponse.fromEntity(
                buscarCupomValidoDoParceiro(parceiroId, qrCodeUnico, emailAutenticado));
    }

    /**
     * O id na URL (/api/usuarios/{usuarioId}/...) vem do cliente e não pode ser
     * confiável por si só — sem essa checagem, qualquer usuário autenticado poderia
     * trocar o id no path e operar cupons de outra pessoa (IDOR). Por isso a
     * identidade real sempre vem do principal autenticado (e-mail do Basic Auth),
     * e o id do path só é aceito se pertencer a esse mesmo principal.
     */
    private Usuario carregarUsuarioAutenticado(Long usuarioId, String emailAutenticado) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("usuário não encontrado"));

        if (!usuario.getEmail().equalsIgnoreCase(emailAutenticado)) {
            throw new AccessDeniedException("usuário só pode acessar os próprios cupons");
        }

        return usuario;
    }

    /** Mesma razão de carregarUsuarioAutenticado, mas para o lado do parceiro. */
    private Parceiro carregarParceiroAutenticado(Long parceiroId, String emailAutenticado) {
        Parceiro parceiro = parceiroRepository.findById(parceiroId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("parceiro não encontrado"));

        if (!parceiro.getEmail().equalsIgnoreCase(emailAutenticado)) {
            throw new AccessDeniedException("parceiro só pode validar cupons do próprio estabelecimento");
        }

        return parceiro;
    }

    private Cupom buscarCupomValidoDoParceiro(Long parceiroId, String qrCodeUnico, String emailAutenticado) {
        Parceiro parceiro = carregarParceiroAutenticado(parceiroId, emailAutenticado);

        Cupom cupom = cupomRepository.findByQrCodeUnico(qrCodeUnico)
                .orElseThrow(() -> new RecursoNaoEncontradoException("cupom inexistente"));

        expirarSeNecessario(cupom);

        if (!cupom.getParceiro().getId().equals(parceiro.getId())) {
            throw new RegraNegocioException("cupom não pertence ao parceiro logado");
        }
        if (cupom.getStatus() == StatusCupom.UTILIZADO) {
            throw new RegraNegocioException("cupom já utilizado");
        }
        if (cupom.getStatus() == StatusCupom.EXPIRADO) {
            throw new RegraNegocioException("cupom expirado");
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
