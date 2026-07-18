package com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command.start;

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
 * Caso de uso para transicao REOPENED → IN_PROGRESS.
 *
 * <p>Retoma o atendimento de uma pendencia apos ela ter sido reaberta pelo SENAI.
 * Apenas WEG e SENAI podem reiniciar o atendimento. A transicao e validada pelo
 * dominio — qualquer outro status resulta em {@code InvalidIssueTransitionException}
 * (HTTP 422).</p>
 */
@Service
@RequiredArgsConstructor
public class RestartProgressIssueUseCase {

    private final ChecklistIssueRepositoryPort repository;
    private final RequestContextProvider contextProvider;

    @Transactional
    public ChecklistIssue execute(UUID issueId) {
        if (!contextProvider.getRequestContext().canManageIssues()) {
            throw new AccessDeniedException("Apenas SENAI e WEG podem retomar o atendimento de pendencias.");
        }

        ChecklistIssue issue = repository.findById(issueId)
                .orElseThrow(() -> new EntityNotFoundException("Pendencia nao encontrada."));

        issue.restartProgress();

        return repository.save(issue);
    }
}
