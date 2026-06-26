package com.awpy.awpy.model;

import com.awpy.awpy.model.enums.StatusCupom;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Entity
@Table(name = "cupom")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cupom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "beneficio_id", nullable = false)
    private Beneficio beneficio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parceiro_id", nullable = false)
    private Parceiro parceiro;

    @Column(nullable = false, unique = true)
    private String qrCodeUnico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusCupom status;

    @Column(nullable = false)
    private LocalDateTime dataGeracao;

    @Column(nullable = false)
    private LocalDateTime dataExpiracao;

    private LocalDateTime dataUtilizacao;
}
