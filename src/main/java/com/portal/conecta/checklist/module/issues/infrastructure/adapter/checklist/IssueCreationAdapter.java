package com.portal.conecta.checklist.module.issues.infrastructure.adapter.checklist;

import com.portal.conecta.checklist.module.checklist.application.port.out.issue.CreateNonComplianceIssueCommand;
import com.portal.conecta.checklist.module.checklist.application.port.out.issue.IssueCreationPort;
import com.portal.conecta.checklist.module.issues.domain.valueobject.UserReference;
import com.portal.conecta.checklist.module.issues.application.port.out.persistence.ChecklistIssueRepositoryPort;
import com.portal.conecta.checklist.module.issues.domain.enums.IssuePriority;
import com.portal.conecta.checklist.module.issues.domain.enums.IssueStatus;
import com.portal.conecta.checklist.module.issues.domain.model.ChecklistIssue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador do modulo Issues para {@link IssueCreationPort} (dono: modulo
 * Checklist) — ver ADR-0020.
 *
 * <p>Possui as regras de criacao de pendencia (prazo de 7 dias, truncamento de
 * colunas) — sao regras do modulo Issues, nao do Checklist.</p>
 */
@Component
@RequiredArgsConstructor
public class IssueCreationAdapter implements IssueCreationPort {

    private static final int ISSUE_DUE_DAYS = 7;

    private final ChecklistIssueRepositoryPort issueRepository;

    @Override
    public Set<String> existingItemKeysForExecution(UUID executionId) {
        return issueRepository.findAllByExecutionId(executionId).stream()
                .map(ChecklistIssue::getItemKey)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> lockedItemKeys(UUID executionId) {
        return issueRepository.findAllByExecutionId(executionId).stream()
                .filter(issue -> issue.getStatus() != IssueStatus.VALIDATED && issue.getStatus() != IssueStatus.CANCELED)
                .map(ChecklistIssue::getItemKey)
                .collect(Collectors.toSet());
    }

    @Override
    public void createNonComplianceIssue(CreateNonComplianceIssueCommand command) {
        Instant dueAt = Instant.now().plusSeconds(ISSUE_DUE_DAYS * 24L * 60L * 60L);

        issueRepository.save(ChecklistIssue.builder()
                .executionId(command.executionId())
                .assignedUserReference(new UserReference(command.assignedUserId()))
                .itemKey(command.itemKey())
                .title(truncate("Pendencia: " + command.itemTitle(), 100))
                .description(truncate(command.observation(), 500))
                .status(IssueStatus.OPEN)
                .priority(IssuePriority.MEDIUM)
                .dueAt(dueAt)
                .build());
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
