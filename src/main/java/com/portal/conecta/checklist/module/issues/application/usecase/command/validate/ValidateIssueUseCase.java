package com.portal.conecta.checklist.module.issues.application.usecase.command.validate;

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
 * Caso de uso para transicao RESOLVED → VALIDATED.
 *
 * <p>Apenas perfis gerenciais (SENAI, WEG) podem validar a resolucao de uma
 * pendencia. A transicao e validada pelo dominio — qualquer outro status resulta em
 * {@code InvalidIssueTransitionException} (HTTP 422).</p>
 */
@Service
@RequiredArgsConstructor
public class ValidateIssueUseCase {

    private final ChecklistIssueRepositoryPort repository;
    private final RequestContextProvider contextProvider;

    @Transactional
    public ChecklistIssue execute(UUID issueId) {
        if (!contextProvider.getRequestContext().canValidateOrReopenIssues()) {
            throw new AccessDeniedException("Apenas perfis gerenciais podem validar a resolucao de pendencias.");
        }

        ChecklistIssue issue = repository.findById(issueId)
                .orElseThrow(() -> new EntityNotFoundException("Pendencia nao encontrada."));

        issue.validate();

        return repository.save(issue);
    }
}

