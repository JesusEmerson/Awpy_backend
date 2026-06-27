package com.awpy.awpy.dto.parceiro;

import com.awpy.awpy.model.Parceiro;

/** Versão leve do parceiro pra listas/dropdowns — não expõe e-mail, CNPJ etc. */
public record ParceiroResumoResponse(
        Long id,
        String nomeEstabelecimento
) {
    public static ParceiroResumoResponse fromEntity(Parceiro parceiro) {
        return new ParceiroResumoResponse(parceiro.getId(), parceiro.getNomeEstabelecimento());
    }
}
