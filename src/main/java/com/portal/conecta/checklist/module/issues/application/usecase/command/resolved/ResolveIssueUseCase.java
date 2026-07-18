package com.portal.conecta.checklist.module.issues.application.usecase.command.resolved;

import com.portal.conecta.checklist.module.issues.domain.model.ChecklistIssue;
import com.portal.conecta.checklist.module.issues.application.port.out.persistence.ChecklistIssueRepositoryPort;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso para transicao IN_PROGRESS → RESOLVED.
 *
 * <p>Apenas WEG e SENAI podem resolver pendencias. A transicao e validada pelo
 * dominio — qualquer outro status resulta em {@code InvalidIssueTransitionException}
 * (HTTP 422).</p>
 */
@Service
@RequiredArgsConstructor
public class ResolveIssueUseCase {

    private final ChecklistIssueRepositoryPort repository;
    private final RequestContextProvider contextProvider;

    @Transactional
    public ChecklistIssue execute(UUID issueId) {
        if (!contextProvider.getRequestContext().canManageIssues()) {
            throw new AccessDeniedException("Apenas SENAI e WEG podem resolver pendencias.");
        }

        ChecklistIssue issue = repository.findById(issueId)
                .orElseThrow(() -> new EntityNotFoundException("Pendencia nao encontrada."));

        issue.resolve();

        return repository.save(issue);
    }
}
