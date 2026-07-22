package com.portal.conecta.checklist.module.checklist.infrastructure.adapter.issues;

import com.portal.conecta.checklist.module.checklist.application.port.out.persistence.ChecklistExecutionRepositoryPort;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.issues.application.port.out.execution.ExecutionAccessPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador do modulo Checklist para {@link ExecutionAccessPort} (dono: modulo
 * Issues) — ver ADR-0020.
 */
@Component
@RequiredArgsConstructor
public class ExecutionAccessAdapter implements ExecutionAccessPort {

    private final ChecklistExecutionRepositoryPort executionRepository;

    @Override
    public Optional<UUID> findClassIdByExecutionId(UUID executionId) {
        return executionRepository.findById(executionId).map(ChecklistExecution::getClassId);
    }
}
