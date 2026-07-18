package com.portal.conecta.checklist.module.checklist.application.usecase.template.query.search;

import java.util.UUID;

/**
 * Resultado de aplicacao da busca de itens por categoria. Vive na camada
 * application para nao acoplar o caso de uso a DTOs de apresentacao — o
 * controller mapeia este resultado para o DTO de resposta.
 */
public record ChecklistItemByCategoryResult(
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
