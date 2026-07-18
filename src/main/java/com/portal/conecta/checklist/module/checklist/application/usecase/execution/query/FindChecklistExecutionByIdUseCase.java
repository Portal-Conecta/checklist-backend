package com.portal.conecta.checklist.module.checklist.application.usecase.execution.query;

import com.portal.conecta.checklist.module.checklist.application.port.out.persistence.ChecklistExecutionRepositoryPort;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindChecklistExecutionByIdUseCase {

    private final ChecklistExecutionRepositoryPort repositoryPort;
    private final RequestContextProvider contextProvider;

    @Transactional(readOnly = true)
    public ChecklistExecution execute(UUID executionId) {
        ChecklistExecution execution = repositoryPort.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execução de checklist não encontrada para o ID informado"));
        
        var context = contextProvider.getRequestContext();
        if (!context.canManageChecklistTemplates() && !context.canOperateChecklistExecutionForClass(execution.getClassId())) {
            throw new AccessDeniedException("Usuário não tem permissão para acessar esta execução de checklist.");
        }

        return execution;
    }
}
