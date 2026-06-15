package com.portal.conecta.checklist.modules.checklist.application.usecase.template.command;

import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSchema;

import java.util.UUID;

public record CreateChecklistTemplateCommand(
        UUID roomId,
        String title,
        String description,
        ChecklistSchema schema
) {
}
