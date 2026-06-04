package com.portal.conecta.checklist.module.checklist.application.facade;


import com.portal.conecta.checklist.module.checklist.application.usecase.execution.*;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChecklistExecutionFacade {

private final CreateChecklistExecutionUseCase createChecklistExecutionUseCase;
private final SubmitChecklistExecutionUseCase submitChecklistExecutionUseCase;
private final ChecklistExecutionMapper executionMapper;
private final CancelChecklistExecutionUseCase cancelChecklistExecutionUseCase;
private final FindChecklistExecutionByIdUseCase findExecutionByIdUseCase;
private final ChecklistExecutionMapper checklistExecutionMapper;
private final ListChecklistExecutionUseCase listChecklistExecutionUseCase;

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
    public ChecklistExecutionResponseDTO findExecutionById(UUID executionId) {
        var execution = findExecutionByIdUseCase.execute(executionId);
        return checklistExecutionMapper.toResponse(execution);
    }
    public Page<ChecklistExecutionResponseDTO> listExecution(Pageable pageable){
        return listChecklistExecutionUseCase.execute(pageable)
                .map(checklistExecutionMapper::toResponse);
    }
}
