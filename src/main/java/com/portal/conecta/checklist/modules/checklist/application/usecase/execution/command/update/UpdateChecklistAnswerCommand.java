package com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.update;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ConformityAnswerValue;

import java.time.Instant;

public record UpdateChecklistAnswerCommand(
        String itemKey,
        ConformityAnswerValue value,
        String observation,
        Instant answeredAt
) {
}
