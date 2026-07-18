package com.portal.conecta.checklist.module.checklist.application.usecase.window.command.upsert;

import java.time.LocalTime;

public record UpsertSubmissionWindowCommand(
        LocalTime openAt,
        int durationMinutes
) {
}
