package com.portal.conecta.checklist.modules.checklist.application.usecase.execution.query;

import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistExecutionRepositoryPort;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindChecklistExecutionByIdUseCase {

    private final ChecklistExecutionRepositoryPort repositoryPort;

    @Transactional(readOnly = true)
    public ChecklistExecution execute(UUID executionId) {
        return repositoryPort.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execução de checklist não encontrada para o ID informado"));
    }
}
