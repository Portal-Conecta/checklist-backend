package com.portal.conecta.checklist.shared.exception;

public record ApiError(
        String timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
