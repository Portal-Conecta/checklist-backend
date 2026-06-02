package com.portal.conecta.checklist.module.issues.presentation.response;

import com.portal.conecta.checklist.module.issues.domain.enums.IssuePriority;
import com.portal.conecta.checklist.module.issues.domain.enums.IssueStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de resposta para uma issue de checklist.
 *
 * <p>Expoe identificadores, item afetado, descricao, prioridade, status e
 * datas relevantes da pendencia.</p>
 */
public record ChecklistIssueResponseDTO(
        UUID id,
        UUID executionId,
        String itemKey,
        String itemTitleSnapshot,
        UUID assignedTo,
        String title,
        String description,
        IssueStatus status,
        IssuePriority priority,
        Instant dueAt,
        Instant resolvedAt
) {}
