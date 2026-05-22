package com.portal.conecta.checklist.module.issues.presentation.response;

import com.portal.conecta.checklist.module.issues.domain.enums.IssuePriority;
import com.portal.conecta.checklist.module.issues.domain.enums.IssueStatus;

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
