package com.awpy.awpy.controller.exception;

import com.awpy.awpy.service.exception.RecursoNaoEncontradoException;
import com.awpy.awpy.service.exception.RegraNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidacao(MethodArgumentNotValidException ex) {
        List<String> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .toList();

        ApiErrorResponse body = new ApiErrorResponse(
                LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "dados inválidos", null, erros);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ApiErrorResponse> handleRegraNegocio(RegraNegocioException ex) {
        ApiErrorResponse body = new ApiErrorResponse(
                LocalDateTime.now(), HttpStatus.CONFLICT.value(), ex.getMessage(), ex.getCodigo(), List.of());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        ApiErrorResponse body = new ApiErrorResponse(
                LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), ex.getMessage(), null, List.of());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleCredenciaisInvalidas(BadCredentialsException ex) {
        ApiErrorResponse body = new ApiErrorResponse(
                LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(), "e-mail ou senha inválidos",
                "CREDENCIAIS_INVALIDAS", List.of());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }
}
