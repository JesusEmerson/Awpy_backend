package com.awpy.awpy.repository;

import com.awpy.awpy.model.AdminFuncionario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminFuncionarioRepository extends JpaRepository<AdminFuncionario, Long> {

    Optional<AdminFuncionario> findByEmail(String email);

    boolean existsByEmail(String email);
}
