package com.portal.conecta.checklist.modules.checklist.application.usecase.template.command;

import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSchema;

public record EditChecklistTemplateCommand(
        String title,
        String description,
        ChecklistSchema schema
) {
}
