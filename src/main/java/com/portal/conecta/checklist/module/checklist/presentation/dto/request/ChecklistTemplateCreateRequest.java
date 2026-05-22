package com.portal.conecta.checklist.module.checklist.presentation.dto.request;

import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;

import java.util.UUID;

public record ChecklistTemplateCreateRequest(
        UUID roomId,
        String title,
        String description,
        ChecklistSchemaDTO schemaJson
) {
}
