package com.portal.conecta.checklist.module.issues.presentation.mapper;

import com.portal.conecta.checklist.module.checklist.domain.valueobject.UserReference;
import com.portal.conecta.checklist.module.issues.domain.model.ChecklistIssue;
import com.portal.conecta.checklist.module.issues.presentation.response.ChecklistIssueResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ChecklistIssueMapper {

    public ChecklistIssueResponseDTO toResponse(ChecklistIssue issue) {
        if (issue == null) {
            return null;
        }

        return new ChecklistIssueResponseDTO(
                issue.getId(),
                issue.getChecklistExecution() == null ? null : issue.getChecklistExecution().getId(),
                issue.getItemKey(),
                issue.getItemTitleSnapshot(),
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
