package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CancelChecklistExecutionUseCase {


    private final ChecklistExecutionRepository executionRepository;


    @Transactional
    public ChecklistExecution execute(UUID executionId){
        ChecklistExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execucao de checklist nao encontrada"));

        if(execution.getStatus() != ChecklistExecutionStatus.SUBMITTED){
            throw new IllegalArgumentException("Somente checklist enviados podem ser cancelados");
        }
        execution.setStatus(ChecklistExecutionStatus.CANCELED);

        return executionRepository.save(execution);


    }


}
