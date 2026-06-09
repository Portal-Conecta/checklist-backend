package com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.update;


import com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.core.ChecklistExecutionScoringService;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateChecklistExecutionAnswersUseCase {


    private final ChecklistExecutionRepository repository;
    private final ChecklistExecutionMapper executionMapper;
    private final RequestContextProvider contextProvider;
    private final ChecklistExecutionScoringService scoringService;


    @Transactional
    public ChecklistExecution execute(UUID executionId, ChecklistExecutionSubmitDTO request){

        ChecklistExecution execution = repository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execucao de checklist não encontrada: " + executionId));


        var currentUser = contextProvider.getRequestContext();

        if(!currentUser.canManageChecklistTemplates()
                && !currentUser.canOperateChecklistExecutionForClass(execution.getClassId())){
                throw new AccessDeniedException("Usuario não tem permissao para editar esta execucao de checklist.");
        }

        if (execution.getStatus() != ChecklistExecutionStatus.SUBMITTED){
            throw new IllegalStateException("Somente checklists que foram enviadas podem ser editadas.");

        }

        execution.setAnswersJson(executionMapper.toAnswersJson(request));
        execution.setComplianceScore(scoringService.calculateComplianceScore(request.answers()));


        return repository.save(execution);


    }


}
