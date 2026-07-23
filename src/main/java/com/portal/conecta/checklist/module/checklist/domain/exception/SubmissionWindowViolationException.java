package com.portal.conecta.checklist.module.checklist.domain.exception;

import java.time.LocalTime;

public class SubmissionWindowViolationException extends RuntimeException {

    public SubmissionWindowViolationException(LocalTime openAt, LocalTime closeAt) {
        super("O horário de envio para esta turma é das " + openAt + " às " + closeAt + ".");
    }
}
