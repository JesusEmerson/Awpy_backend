package com.awpy.awpy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tipo de material reciclável (ex.: Ferro, Cobre), com pontuação por Kg definida
 * pelo admin. "ativo" existe pra poder descontinuar um material sem apagar o
 * histórico de reciclagens antigas que o referenciam (FK não pode virar órfã).
 */
@Entity
@Table(name = "material")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    @Column(nullable = false)
    private Double pontosPorKg;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;
}
