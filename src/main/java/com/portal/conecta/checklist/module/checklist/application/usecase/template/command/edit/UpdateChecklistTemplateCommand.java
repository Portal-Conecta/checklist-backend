package com.portal.conecta.checklist.module.checklist.application.usecase.template.command.edit;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistCategory;
import com.portal.conecta.checklist.module.checklist.domain.schema.ChecklistSchema;

public record UpdateChecklistTemplateCommand(
        String title,
        String description,
        ChecklistCategory category,
        ChecklistSchema schema
) {
}
