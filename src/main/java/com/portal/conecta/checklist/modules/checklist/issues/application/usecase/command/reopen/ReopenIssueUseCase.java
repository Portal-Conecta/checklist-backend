package com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command.reopen;

import com.portal.conecta.checklist.modules.checklist.issues.domain.model.ChecklistIssue;
import com.portal.conecta.checklist.modules.checklist.issues.application.port.out.persistence.ChecklistIssueRepositoryPort;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.context.TypeUser;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso para transicao RESOLVED → REOPENED.
 *
 * <p>Apenas SENAI pode rejeitar uma resolucao e reabrir a pendencia. A transicao
 * e validada pelo dominio — qualquer outro status resulta em
 * {@code InvalidIssueTransitionException} (HTTP 422).</p>
 */
@Service
@RequiredArgsConstructor
public class ReopenIssueUseCase {

    private final ChecklistIssueRepositoryPort repository;
    private final RequestContextProvider contextProvider;

    @Transactional
    public ChecklistIssue execute(UUID issueId) {
        if (!contextProvider.getRequestContext().canOnlySenaiManageIssues()) {
            throw new AccessDeniedException("Apenas SENAI pode reabrir pendencias.");
        }

        ChecklistIssue issue = repository.findById(issueId)
                .orElseThrow(() -> new EntityNotFoundException("Pendencia nao encontrada."));

        issue.reopen();

        return repository.save(issue);
    }
}



