package com.portal.conecta.checklist.modules.checklist.presentation.dto.template.response;

public record ChecklistItemSearchResponseDTO(
        String key,
        String title,
        String description,
        Boolean required,
        Integer order
) {}
