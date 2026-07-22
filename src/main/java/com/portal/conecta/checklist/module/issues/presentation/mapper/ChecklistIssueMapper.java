package com.portal.conecta.checklist.module.issues.presentation.mapper;

import com.portal.conecta.checklist.module.issues.domain.valueobject.UserReference;
import com.portal.conecta.checklist.module.issues.domain.model.ChecklistIssue;
import com.portal.conecta.checklist.module.issues.presentation.dto.response.ChecklistIssueResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Mapper de apresentacao para issues de checklist.
 *
 * <p>Converte entidades de issue para DTOs de resposta usados por endpoints e
 * consumidores externos.</p>
 */
@Component
public class ChecklistIssueMapper {

    public ChecklistIssueResponseDTO toResponse(ChecklistIssue issue) {
        if (issue == null) {
            return null;
        }

        return new ChecklistIssueResponseDTO(
                issue.getId(),
                issue.getExecutionId(),
                issue.getItemKey(),
                assignedTo(issue.getAssignedUserReference()),
                issue.getTitle(),
                issue.getDescription(),
                issue.getStatus(),
                issue.getPriority(),
                issue.getDueAt(),
                issue.getResolvedAt()
        );
    }

    public List<ChecklistIssueResponseDTO> toResponseList(List<ChecklistIssue> issues) {
        if (issues == null || issues.isEmpty()) {
            return List.of();
        }

        return issues.stream()
                .map(this::toResponse)
                .toList();
    }

    private UUID assignedTo(UserReference assignedUserReference) {
        return assignedUserReference == null ? null : assignedUserReference.getUserId();
    }
}
