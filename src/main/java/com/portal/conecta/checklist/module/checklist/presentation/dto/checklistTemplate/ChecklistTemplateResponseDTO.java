package com.portal.conecta.checklist.module.checklist.presentation.dto.checklistTemplate;

import com.portal.conecta.checklist.module.checklist.domain.model.Status;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.RoomReference;
import com.portal.conecta.checklist.module.checklist.presentation.dto.UserToken;

import java.util.UUID;

public record ChecklistTemplateResponseDTO(
        UUID id,
        RoomReference roomReference,
        String title,
        String description,
        int version,
        Status status,
        boolean active,
        UserToken schemaJson
) {
}
