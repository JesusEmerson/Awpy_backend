package com.awpy.awpy.service.exception;

/**
 * Lançada quando uma entidade buscada por id não existe.
 * Tratada pelo GlobalExceptionHandler e traduzida para HTTP 404.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String message) {
        super(message);
    }
}
