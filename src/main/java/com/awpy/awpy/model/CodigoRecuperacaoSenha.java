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

import java.time.LocalDateTime;

/**
 * Código de 6 dígitos pra redefinição de senha. Não amarrado a um perfil específico
 * (usuário/parceiro/admin) — o e-mail é que decide isso na hora de redefinir, igual
 * ao login único.
 *
 * MODO DEMO: hoje o código não é enviado por e-mail (não há SMTP configurado), ele
 * volta direto na resposta de POST /api/auth/recuperar-senha. Isso é proposital pra
 * testar o fluxo sem precisar de infraestrutura de e-mail agora — nunca fazer isso
 * num ambiente real, é uma falha de segurança grave (qualquer um redefine a senha de
 * qualquer e-mail sem provar que tem acesso a ele).
 */
@Entity
@Table(name = "codigo_recuperacao_senha")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodigoRecuperacaoSenha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String codigo;

    @Column(nullable = false)
    private LocalDateTime dataExpiracao;

    @Column(nullable = false)
    @Builder.Default
    private Boolean usado = false;
}
