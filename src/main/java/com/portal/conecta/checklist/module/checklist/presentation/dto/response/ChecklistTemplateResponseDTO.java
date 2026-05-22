package com.portal.conecta.checklist.module.checklist.presentation.dto.response;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;

import java.time.Instant;
import java.util.UUID;

public record ChecklistTemplateResponseDTO(
        UUID id,
        UUID roomId,
        String title,
        String description,
        Integer version,
        ChecklistTemplateStatus status,
        Boolean active,
        ChecklistSchemaDTO schemaJson,
        Instant createdAt,
        Instant updatedAt
) {}
