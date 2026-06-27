package com.awpy.awpy.repository;

import com.awpy.awpy.model.Reciclagem;
import com.awpy.awpy.model.Usuario;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReciclagemRepository extends JpaRepository<Reciclagem, Long> {

    @Query("SELECT COALESCE(SUM(r.quilos), 0) FROM Reciclagem r WHERE r.usuario = :usuario")
    Double somarQuilosPorUsuario(@Param("usuario") Usuario usuario);

    @Query("SELECT COALESCE(SUM(r.quilos), 0) FROM Reciclagem r")
    Double somarTodosOsQuilos();

    @Query("""
            SELECT r.usuario.id AS usuarioId, r.usuario.nomeCompleto AS nome, SUM(r.quilos) AS totalKg
            FROM Reciclagem r
            GROUP BY r.usuario.id, r.usuario.nomeCompleto
            ORDER BY SUM(r.quilos) DESC
            """)
    List<UsuarioKgProjection> rankingUsuariosPorKg(Pageable pageable);

    @Query("""
            SELECT r.parceiro.id AS parceiroId, r.parceiro.nomeEstabelecimento AS nomeEstabelecimento, SUM(r.quilos) AS totalKg
            FROM Reciclagem r
            GROUP BY r.parceiro.id, r.parceiro.nomeEstabelecimento
            ORDER BY SUM(r.quilos) DESC
            """)
    List<ParceiroKgProjection> rankingParceirosPorKg(Pageable pageable);
}
