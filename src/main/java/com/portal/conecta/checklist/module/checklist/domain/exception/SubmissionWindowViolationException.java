package com.portal.conecta.checklist.module.checklist.domain.exception;

import java.time.LocalTime;

public class SubmissionWindowViolationException extends RuntimeException {

    public SubmissionWindowViolationException(LocalTime openAt, LocalTime closeAt) {
        super("Fora da janela de envio. Janela permitida: " + openAt + " ate " + closeAt + ".");
    }
}
