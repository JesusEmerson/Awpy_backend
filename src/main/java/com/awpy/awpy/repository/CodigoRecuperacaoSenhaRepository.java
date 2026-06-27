package com.awpy.awpy.repository;

import com.awpy.awpy.model.CodigoRecuperacaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodigoRecuperacaoSenhaRepository extends JpaRepository<CodigoRecuperacaoSenha, Long> {

    Optional<CodigoRecuperacaoSenha> findByEmailAndCodigoAndUsadoFalse(String email, String codigo);
}
