package com.awpy.awpy.service;

import com.awpy.awpy.dto.material.MaterialCadastroRequest;
import com.awpy.awpy.dto.material.MaterialResponse;
import com.awpy.awpy.dto.material.MaterialUpdateRequest;
import com.awpy.awpy.model.Material;
import com.awpy.awpy.repository.MaterialRepository;
import com.awpy.awpy.service.exception.RecursoNaoEncontradoException;
import com.awpy.awpy.service.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;

    /** Usado pelo parceiro ao registrar uma reciclagem — só material ainda em uso. */
    public List<MaterialResponse> listarAtivos() {
        return materialRepository.findByAtivoTrue().stream()
                .map(MaterialResponse::fromEntity)
                .toList();
    }

    /** Usado pela tela de gestão do admin — precisa ver inativos pra poder reativar. */
    public List<MaterialResponse> listarTodos() {
        return materialRepository.findAll().stream()
                .map(MaterialResponse::fromEntity)
                .toList();
    }

    @Transactional
    public MaterialResponse cadastrar(MaterialCadastroRequest request) {
        if (materialRepository.existsByNome(request.nome())) {
            throw new RegraNegocioException("material já cadastrado", "MATERIAL_JA_CADASTRADO");
        }

        Material material = Material.builder()
                .nome(request.nome())
                .pontosPorKg(request.pontosPorKg())
                .ativo(true)
                .build();

        return MaterialResponse.fromEntity(materialRepository.save(material));
    }

    @Transactional
    public MaterialResponse editar(Long materialId, MaterialUpdateRequest request) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("material não encontrado"));

        material.setNome(request.nome());
        material.setPontosPorKg(request.pontosPorKg());
        material.setAtivo(request.ativo());

        return MaterialResponse.fromEntity(materialRepository.save(material));
    }
}
