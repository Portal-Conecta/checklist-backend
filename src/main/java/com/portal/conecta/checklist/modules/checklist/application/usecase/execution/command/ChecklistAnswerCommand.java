package com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ConformityAnswerValue;

import java.time.Instant;

public record ChecklistAnswerCommand(
        String itemKey,
        ConformityAnswerValue value,
        String observation,
        Instant answeredAt
) {
}
