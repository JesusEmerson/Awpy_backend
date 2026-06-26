package com.awpy.awpy.repository;

import com.awpy.awpy.model.Beneficio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BeneficioRepository extends JpaRepository<Beneficio, Long> {

    List<Beneficio> findByAtivoTrue();
}
