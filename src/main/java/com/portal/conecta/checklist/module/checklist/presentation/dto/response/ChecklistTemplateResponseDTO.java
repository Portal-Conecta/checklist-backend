package com.portal.conecta.checklist.module.checklist.presentation.dto.response;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de resposta para templates de checklist.
 *
 * <p>Exp?e dados de versionamento, status, sala e schema do template sem
 * acoplar clientes a entidade JPA.</p>
 */
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
