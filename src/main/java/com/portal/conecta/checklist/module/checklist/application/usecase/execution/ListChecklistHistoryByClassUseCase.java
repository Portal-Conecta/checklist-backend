package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionHistoryDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListChecklistHistoryByClassUseCase {

    private final ChecklistExecutionRepository repository;
    private final ChecklistExecutionMapper executionMapper;
    private final RequestContextProvider contextProvider;

    public List<ChecklistExecutionHistoryDTO> execute(UUID classId) {
        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canManageChecklistTemplates() && !currentUser.canOperateChecklistExecutionForClass(classId)) {
            throw new AccessDeniedException("Usuario nao tem permissao para consultar o historico desta turma.");
        }

        var executions = repository.findByClassIdAndStatusOrderBySubmittedAtDesc(
                classId,
                ChecklistExecutionStatus.SUBMITTED
        );

        return executionMapper.toHistoryResponseList(executions);
    }
}
