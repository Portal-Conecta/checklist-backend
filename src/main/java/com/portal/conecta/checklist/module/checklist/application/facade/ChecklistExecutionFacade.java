package com.portal.conecta.checklist.module.checklist.application.facade;


import com.portal.conecta.checklist.module.checklist.application.usecase.execution.CancelChecklistExecutionUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.CreateChecklistExecutionUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.SubmitChecklistExecutionUseCase;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChecklistExecutionFacade {

private final CreateChecklistExecutionUseCase createChecklistExecutionUseCase;
private final SubmitChecklistExecutionUseCase submitChecklistExecutionUseCase;
private final ChecklistExecutionMapper executionMapper;
private final CancelChecklistExecutionUseCase cancelChecklistExecutionUseCase;

    public ChecklistExecutionResponseDTO createDTO(ChecklistExecutionDraftCreateDTO request){

        ChecklistExecution execution = createChecklistExecutionUseCase.execute(request);
        return  executionMapper.toResponse(execution);
    }

    public ChecklistExecutionResponseDTO submit(UUID executionId, ChecklistExecutionSubmitDTO request) {
        ChecklistExecution execution = submitChecklistExecutionUseCase.execute(executionId, request);
        return executionMapper.toResponse(execution);
    }

    public ChecklistExecutionResponseDTO cancel(UUID executionId){
        ChecklistExecution execution = cancelChecklistExecutionUseCase.execute(executionId);
        return  executionMapper.toResponse(execution);
    }
}
