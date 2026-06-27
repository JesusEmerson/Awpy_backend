package com.awpy.awpy.controller.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String message,
        String codigo,
        List<String> erros
) {
}
