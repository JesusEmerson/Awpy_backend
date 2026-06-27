package com.awpy.awpy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Registro de uma entrega de material reciclável feita por um usuário num parceiro.
 * É o evento que gera os pontos (quilos * Material.pontosPorKg) — guardamos o valor
 * já calculado em pontosCreditados pra não depender de Material.pontosPorKg mudar
 * depois e alterar retroativamente o histórico.
 */
@Entity
@Table(name = "reciclagem")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reciclagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parceiro_id", nullable = false)
    private Parceiro parceiro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(nullable = false)
    private Double quilos;

    @Column(nullable = false)
    private Long pontosCreditados;

    @Column(nullable = false)
    private LocalDateTime dataHora;
}
