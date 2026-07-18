package com.portal.conecta.checklist.module.checklist.application.usecase.template.command.create;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistCategory;
import com.portal.conecta.checklist.module.checklist.domain.schema.ChecklistSchema;

import java.util.UUID;

public record CreateChecklistTemplateCommand(
        UUID roomId,
        String title,
        String description,
        ChecklistCategory category,
        ChecklistSchema schema
) {
}
