package com.awpy.awpy.repository;

import com.awpy.awpy.model.Cupom;
import com.awpy.awpy.model.Usuario;
import com.awpy.awpy.model.enums.StatusCupom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CupomRepository extends JpaRepository<Cupom, Long> {

    Optional<Cupom> findByQrCodeUnico(String qrCodeUnico);

    List<Cupom> findByUsuarioAndStatus(Usuario usuario, StatusCupom status);

    long countByUsuarioAndStatus(Usuario usuario, StatusCupom status);

    long countByStatus(StatusCupom status);

    List<Cupom> findByUsuarioOrderByDataGeracaoDesc(Usuario usuario);
}
