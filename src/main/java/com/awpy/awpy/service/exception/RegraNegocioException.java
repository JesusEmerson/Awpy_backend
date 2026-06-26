package com.awpy.awpy.service.exception;

/**
 * Lançada quando uma regra de negócio é violada (ex.: duplicidade de cadastro).
 * Tratada pelo GlobalExceptionHandler e traduzida para HTTP 409.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String message) {
        super(message);
    }
}
