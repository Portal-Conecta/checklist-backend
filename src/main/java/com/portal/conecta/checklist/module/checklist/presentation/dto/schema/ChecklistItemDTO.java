package com.portal.conecta.checklist.module.checklist.presentation.dto.schema;

public record ChecklistItemDTO(
        String key,
        String title,
        String description,
        Boolean required,
        Integer order
) {}