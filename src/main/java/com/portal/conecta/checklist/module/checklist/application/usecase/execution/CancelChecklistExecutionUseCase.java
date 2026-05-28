package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
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
public class CancelChecklistExecutionUseCase {


    private final ChecklistExecutionRepository executionRepository;
    private final RequestContextProvider contextProvider;


    @Transactional
    public ChecklistExecution execute(UUID executionId){
        ChecklistExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execucao de checklist nao encontrada"));

        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canCancelChecklistExecution(execution.getUserId(), execution.getClassId())) {
            throw new AccessDeniedException("Usuario nao tem permissao para cancelar esta execucao de checklist.");
        }

        if(execution.getStatus() != ChecklistExecutionStatus.SUBMITTED){
            throw new IllegalArgumentException("Somente checklist enviados podem ser cancelados");
        }
        execution.setStatus(ChecklistExecutionStatus.CANCELED);

        return executionRepository.save(execution);


    }


}
