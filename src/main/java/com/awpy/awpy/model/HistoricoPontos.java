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
 * Registra cada crédito de pontos ganho por um usuário, com a data exata. Existe
 * separado de Usuario.saldoPontos porque o saldo é o valor trocável por benefícios
 * (nunca reseta), enquanto o ranking mensal precisa somar só os pontos ganhos dentro
 * do mês corrente.
 */
@Entity
@Table(name = "historico_pontos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricoPontos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private Long pontos;

    @Column(nullable = false)
    private LocalDateTime dataHora;
}
