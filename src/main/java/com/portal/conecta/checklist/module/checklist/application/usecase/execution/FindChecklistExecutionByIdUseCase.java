package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindChecklistExecutionByIdUseCase {

    private final ChecklistExecutionRepository executionRepository;
    private final RequestContextProvider contextProvider;

    @Transactional(readOnly = true)
    public ChecklistExecution execute(UUID executionId){
        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canAccessChecklistModule()){
            throw new AccessDeniedException("Usuario nao tem permissao para acessar o modulo Checklist.");
        }
        ChecklistExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execução de checklist nao encontrado."));

        if (!currentUser.canManageChecklistTemplates()) {
            if (!execution.getUserId().equals(currentUser.userId())
                    || !currentUser.canOperateChecklistExecutionForClass(execution.getClassId())) {
                throw new AccessDeniedException("Usuario nao tem permissao para acessar esta execucao de checklist.");
            }
        }

        return execution;
    }


}
