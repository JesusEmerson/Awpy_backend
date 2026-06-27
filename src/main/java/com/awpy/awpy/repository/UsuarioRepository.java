package com.awpy.awpy.repository;

import com.awpy.awpy.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByQrCodeUsuario(String qrCodeUsuario);

    boolean existsByEmail(String email);

    boolean existsByCpfCnpj(String cpfCnpj);
}
