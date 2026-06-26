package com.awpy.awpy.service;

import com.awpy.awpy.dto.beneficio.BeneficioCadastroRequest;
import com.awpy.awpy.dto.beneficio.BeneficioResponse;
import com.awpy.awpy.dto.beneficio.BeneficioUpdateRequest;
import com.awpy.awpy.model.Beneficio;
import com.awpy.awpy.model.Parceiro;
import com.awpy.awpy.repository.BeneficioRepository;
import com.awpy.awpy.repository.ParceiroRepository;
import com.awpy.awpy.service.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BeneficioService {

    private final BeneficioRepository beneficioRepository;
    private final ParceiroRepository parceiroRepository;

    public List<BeneficioResponse> listarAtivos() {
        return beneficioRepository.findByAtivoTrue().stream()
                .map(BeneficioResponse::fromEntity)
                .toList();
    }

    @Transactional
    public BeneficioResponse cadastrar(BeneficioCadastroRequest request) {
        Parceiro parceiro = parceiroRepository.findById(request.parceiroId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("parceiro não encontrado"));

        Beneficio beneficio = Beneficio.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .custoEmPontos(request.custoEmPontos())
                .percentualDesconto(request.percentualDesconto())
                .percentualCashback(request.percentualCashback())
                .parceiro(parceiro)
                .ativo(true)
                .build();

        return BeneficioResponse.fromEntity(beneficioRepository.save(beneficio));
    }

    @Transactional
    public BeneficioResponse editar(Long beneficioId, BeneficioUpdateRequest request) {
        Beneficio beneficio = beneficioRepository.findById(beneficioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("benefício não encontrado"));

        beneficio.setNome(request.nome());
        beneficio.setDescricao(request.descricao());
        beneficio.setCustoEmPontos(request.custoEmPontos());
        beneficio.setAtivo(request.ativo());
        beneficio.setPercentualDesconto(request.percentualDesconto());
        beneficio.setPercentualCashback(request.percentualCashback());

        return BeneficioResponse.fromEntity(beneficioRepository.save(beneficio));
    }
}
