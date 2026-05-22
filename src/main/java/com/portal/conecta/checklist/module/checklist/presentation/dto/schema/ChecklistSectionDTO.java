package com.portal.conecta.checklist.module.checklist.presentation.dto.schema;


import java.util.List;

public record ChecklistSectionDTO(
        String key,
        String title,
        Integer order,
        List<ChecklistItemDTO> items
) {}