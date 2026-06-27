package com.awpy.awpy.repository;

import com.awpy.awpy.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    List<Material> findByAtivoTrue();

    boolean existsByNome(String nome);
}
