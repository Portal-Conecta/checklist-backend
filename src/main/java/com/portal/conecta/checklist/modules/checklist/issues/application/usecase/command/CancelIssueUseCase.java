package com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command;

import com.portal.conecta.checklist.modules.checklist.issues.domain.model.ChecklistIssue;
import com.portal.conecta.checklist.modules.checklist.issues.application.port.out.persistence.ChecklistIssueRepositoryPort;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso para transicao para CANCELED (a partir de OPEN ou IN_PROGRESS).
 *
 * <p>Apenas WEG e SENAI podem cancelar pendencias. A transicao e validada pelo
 * dominio — qualquer outro status resulta em {@code InvalidIssueTransitionException}
 * (HTTP 422).</p>
 */
@Service
@RequiredArgsConstructor
public class CancelIssueUseCase {

    private final ChecklistIssueRepositoryPort repository;
    private final RequestContextProvider contextProvider;

    @Transactional
    public ChecklistIssue execute(UUID issueId) {
        if (!contextProvider.getRequestContext().canManageChecklistTemplates()) {
            throw new AccessDeniedException("Apenas SENAI e WEG podem cancelar pendencias.");
        }

        ChecklistIssue issue = repository.findById(issueId)
                .orElseThrow(() -> new EntityNotFoundException("Pendencia nao encontrada."));

        issue.cancel();

        return repository.save(issue);
    }
}
