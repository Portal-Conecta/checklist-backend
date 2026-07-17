package com.portal.conecta.checklist.modules.checklist.presentation.dto.template.response;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistCategory;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSchema;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.shared.RoomResponseDTO;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de resposta para templates de checklist.
 *
 * <p>Expose dados de versionamento, status, sala e schema do template sem
 * acoplar clientes a entidade JPA.</p>
 */
public record ChecklistTemplateResponseDTO(
        UUID id,
        UUID roomId,
        String title,
        String description,
        ChecklistCategory category,
        Integer version,
        ChecklistTemplateStatus status,
        Boolean active,
        ChecklistSchema schemaJson,
        Instant createdAt,
        Instant updatedAt,
        RoomResponseDTO room
) {}
