package com.portal.conecta.checklist.module.issues.application.usecase;

import com.portal.conecta.checklist.module.issues.domain.model.ChecklistIssue;
import com.portal.conecta.checklist.module.issues.infrastructure.persistence.ChecklistIssueRepository;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListIssuesByExecutionUseCase {

    private final ChecklistIssueRepository repository;
    private final RequestContextProvider contextProvider;

    @Transactional(readOnly = true)
    public List<ChecklistIssue> execute(UUID executionId) {
        if (!contextProvider.getRequestContext().canAccessChecklistModule()) {
            throw new AccessDeniedException("Usuario nao tem permissao para acessar o modulo Checklist.");
        }

        return repository.findAllByChecklistExecution_Id(executionId);
    }
}
