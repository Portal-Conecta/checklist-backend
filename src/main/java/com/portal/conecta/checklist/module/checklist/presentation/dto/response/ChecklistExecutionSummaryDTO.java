package com.portal.conecta.checklist.module.checklist.presentation.dto.response;

public record ChecklistExecutionSummaryDTO(
        Integer totalItems,
        Integer answeredItems,
        Integer compliantItems,
        Integer nonCompliantItems
) {}
