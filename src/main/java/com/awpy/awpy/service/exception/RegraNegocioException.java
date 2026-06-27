package com.awpy.awpy.service.exception;

import lombok.Getter;

/**
 * Lançada quando uma regra de negócio é violada (ex.: duplicidade de cadastro).
 * Tratada pelo GlobalExceptionHandler e traduzida para HTTP 409.
 *
 * "codigo" é opcional e existe pra quem consome a API (o app mobile, por exemplo)
 * poder decidir o que fazer sem parsear a mensagem em português — ela pode mudar de
 * texto livremente, o código não.
 */
@Getter
public class RegraNegocioException extends RuntimeException {

    private final String codigo;

    public RegraNegocioException(String message) {
        this(message, null);
    }

    public RegraNegocioException(String message, String codigo) {
        super(message);
        this.codigo = codigo;
    }
}
