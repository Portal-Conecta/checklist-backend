package com.portal.conecta.checklist.module.issues.application.usecase;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.module.issues.domain.model.ChecklistIssue;
import com.portal.conecta.checklist.module.issues.infrastructure.persistence.ChecklistIssueRepository;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import jakarta.persistence.EntityNotFoundException;
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
    private final ChecklistExecutionRepository executionRepository;
    private final RequestContextProvider contextProvider;

    @Transactional(readOnly = true)
    public List<ChecklistIssue> execute(UUID executionId) {
        ChecklistExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execucao de checklist nao encontrada."));

        RequestContext currentUser = contextProvider.getRequestContext();

        if (!currentUser.canManageChecklistTemplates()
                && !currentUser.canOperateChecklistExecutionForClass(execution.getClassId())) {
            throw new AccessDeniedException("Usuario nao tem permissao para consultar pendencias desta execucao.");
        }

        return repository.findAllByChecklistExecution_Id(executionId);
    }
}
