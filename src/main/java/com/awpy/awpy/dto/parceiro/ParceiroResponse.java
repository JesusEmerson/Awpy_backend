package com.awpy.awpy.dto.parceiro;

import com.awpy.awpy.model.Parceiro;

public record ParceiroResponse(
        Long id,
        String nomeEstabelecimento,
        String cnpj,
        String email,
        String telefone,
        String endereco,
        String fotoUrl,
        Double percentualDesconto,
        Double percentualCashback
) {
    public static ParceiroResponse fromEntity(Parceiro parceiro) {
        return new ParceiroResponse(
                parceiro.getId(),
                parceiro.getNomeEstabelecimento(),
                parceiro.getCnpj(),
                parceiro.getEmail(),
                parceiro.getTelefone(),
                parceiro.getEndereco(),
                parceiro.getFotoUrl(),
                parceiro.getPercentualDesconto(),
                parceiro.getPercentualCashback()
        );
    }
}
