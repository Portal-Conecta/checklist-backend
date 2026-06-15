package com.portal.conecta.checklist.modules.checklist.application.usecase.window.command;

import java.time.LocalTime;

public record UpsertSubmissionWindowCommand(
        LocalTime openAt,
        int durationMinutes
) {
}
