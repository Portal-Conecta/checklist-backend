package com.portal.conecta.checklist.module.issues.application.usecase.query;

import com.portal.conecta.checklist.module.issues.application.port.out.execution.ExecutionAccessPort;
import com.portal.conecta.checklist.module.issues.domain.model.ChecklistIssue;
import com.portal.conecta.checklist.module.issues.application.port.out.persistence.ChecklistIssueRepositoryPort;
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

    private final ChecklistIssueRepositoryPort repository;
    private final ExecutionAccessPort executionAccessPort;
    private final RequestContextProvider contextProvider;

    @Transactional(readOnly = true)
    public List<ChecklistIssue> execute(UUID executionId) {
        UUID classId = executionAccessPort.findClassIdByExecutionId(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execucao de checklist nao encontrada."));

        RequestContext currentUser = contextProvider.getRequestContext();

        if (!currentUser.canManageChecklistTemplates()
                && !currentUser.canOperateChecklistExecutionForClass(classId)) {
            throw new AccessDeniedException("Usuario nao tem permissao para consultar pendencias desta execucao.");
        }

        return repository.findAllByExecutionId(executionId);
    }
}
