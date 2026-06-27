package com.awpy.awpy.repository;

import com.awpy.awpy.model.Parceiro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParceiroRepository extends JpaRepository<Parceiro, Long> {

    Optional<Parceiro> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCnpj(String cnpj);
}
