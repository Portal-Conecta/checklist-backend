package com.portal.conecta.checklist.module.checklist.application.facade;


import com.portal.conecta.checklist.module.checklist.application.usecase.execution.CreateChecklistExecutionUseCase;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChecklistExecutionFacade {

private final CreateChecklistExecutionUseCase createChecklistExecutionUseCase;
private final ChecklistExecutionMapper executionMapper;

    public ChecklistExecutionResponseDTO createDTO(ChecklistExecutionDraftCreateDTO request){

        ChecklistExecution execution = createChecklistExecutionUseCase.execute(request);
        return  executionMapper.toResponse(execution);
    }

}
