package com.portal.conecta.checklist.shared.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponseDTO(
        LocalDateTime localDateTime,
        int status,
        String message,
        Map<String, String> errors
) {
}