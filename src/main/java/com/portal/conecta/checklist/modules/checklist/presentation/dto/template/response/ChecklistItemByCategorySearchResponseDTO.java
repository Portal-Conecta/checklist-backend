package com.portal.conecta.checklist.modules.checklist.presentation.dto.template.response;

import java.util.UUID;

/**
 * DTO de resposta para busca de itens por categoria.
 */
public record ChecklistItemByCategorySearchResponseDTO(
        UUID templateId,
        String templateTitle,
        String sectionKey,
        String sectionTitle,
        String key,
        String title,
        String description,
        Boolean required,
        Integer order,
        String category
) {}
