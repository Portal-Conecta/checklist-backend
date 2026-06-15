package com.portal.conecta.checklist.modules.checklist.issues.presentation.dto.response;

import com.portal.conecta.checklist.modules.checklist.issues.domain.enums.IssuePriority;
import com.portal.conecta.checklist.modules.checklist.issues.domain.enums.IssueStatus;

import java.time.Instant;
import java.util.UUID;

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
