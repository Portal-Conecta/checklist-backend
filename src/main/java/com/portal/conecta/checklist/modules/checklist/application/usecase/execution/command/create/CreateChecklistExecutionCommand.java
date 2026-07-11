package com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.create;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistType;

import java.util.UUID;

public record CreateChecklistExecutionCommand(
        UUID templateId,
        UUID roomId,
        UUID classId,
        ChecklistType checklistType
) {
}
