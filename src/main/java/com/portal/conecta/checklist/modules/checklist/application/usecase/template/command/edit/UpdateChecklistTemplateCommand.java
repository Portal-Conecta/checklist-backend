package com.portal.conecta.checklist.modules.checklist.application.usecase.template.command.edit;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistCategory;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSchema;

public record UpdateChecklistTemplateCommand(
        String title,
        String description,
        ChecklistCategory category,
        ChecklistSchema schema
) {
}
