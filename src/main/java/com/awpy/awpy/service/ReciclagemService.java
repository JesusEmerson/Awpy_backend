package com.awpy.awpy.service;

import com.awpy.awpy.dto.reciclagem.IdentificacaoUsuarioResponse;
import com.awpy.awpy.dto.reciclagem.ReciclagemResponse;
import com.awpy.awpy.model.HistoricoPontos;
import com.awpy.awpy.model.Material;
import com.awpy.awpy.model.Parceiro;
import com.awpy.awpy.model.Reciclagem;
import com.awpy.awpy.model.Usuario;
import com.awpy.awpy.repository.HistoricoPontosRepository;
import com.awpy.awpy.repository.MaterialRepository;
import com.awpy.awpy.repository.ParceiroRepository;
import com.awpy.awpy.repository.ReciclagemRepository;
import com.awpy.awpy.repository.UsuarioRepository;
import com.awpy.awpy.service.exception.RecursoNaoEncontradoException;
import com.awpy.awpy.service.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Como o usuário ganha pontos de fato: o parceiro identifica o usuário pelo QR Code
 * da Home dele (Usuario.qrCodeUsuario — nunca o QR do cupom), escolhe o material e
 * informa o peso. Pontos = quilos * Material.pontosPorKg, arredondado.
 */
@Service
@RequiredArgsConstructor
public class ReciclagemService {

    private final UsuarioRepository usuarioRepository;
    private final ParceiroRepository parceiroRepository;
    private final MaterialRepository materialRepository;
    private final ReciclagemRepository reciclagemRepository;
    private final HistoricoPontosRepository historicoPontosRepository;

    public IdentificacaoUsuarioResponse identificarPorQrCode(String qrCodeUsuario) {
        Usuario usuario = usuarioRepository.findByQrCodeUsuario(qrCodeUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("usuário não encontrado para este QR Code"));

        return IdentificacaoUsuarioResponse.fromEntity(usuario);
    }

    @Transactional
    public ReciclagemResponse registrar(String emailParceiroAutenticado, Long usuarioId, Long materialId, Double quilos) {
        Parceiro parceiro = parceiroRepository.findByEmail(emailParceiroAutenticado)
                .orElseThrow(() -> new RecursoNaoEncontradoException("parceiro não encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("usuário não encontrado"));

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("material não encontrado"));

        if (!material.getAtivo()) {
            throw new RegraNegocioException("material indisponível", "MATERIAL_INATIVO");
        }

        long pontosCreditados = Math.round(quilos * material.getPontosPorKg());

        usuario.setSaldoPontos(usuario.getSaldoPontos() + pontosCreditados);
        usuarioRepository.save(usuario);

        historicoPontosRepository.save(HistoricoPontos.builder()
                .usuario(usuario)
                .pontos(pontosCreditados)
                .dataHora(LocalDateTime.now())
                .build());

        reciclagemRepository.save(Reciclagem.builder()
                .usuario(usuario)
                .parceiro(parceiro)
                .material(material)
                .quilos(quilos)
                .pontosCreditados(pontosCreditados)
                .dataHora(LocalDateTime.now())
                .build());

        Double novoTotalKg = reciclagemRepository.somarQuilosPorUsuario(usuario);

        return new ReciclagemResponse(pontosCreditados, usuario.getSaldoPontos(), novoTotalKg);
    }
}
