package com.portal.conecta.checklist.module.checklist.presentation.controller;

import com.portal.conecta.checklist.module.checklist.application.usecase.execution.CancelChecklistExecutionUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.CreateChecklistExecutionUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.SubmitChecklistExecutionUseCase;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChecklistExecutionControllerTest {

    private final CreateChecklistExecutionUseCase createUseCase = mock(CreateChecklistExecutionUseCase.class);
    private final SubmitChecklistExecutionUseCase submitUseCase = mock(SubmitChecklistExecutionUseCase.class);
    private final CancelChecklistExecutionUseCase cancelUseCase = mock(CancelChecklistExecutionUseCase.class);
    private final ChecklistExecutionMapper mapper               = mock(ChecklistExecutionMapper.class);
    private final ChecklistExecutionController controller       = new ChecklistExecutionController(
            createUseCase, submitUseCase, cancelUseCase, mapper);

    @Test
    @DisplayName("deve retornar created ao criar draft")
    void deveRetornarCreatedAoCriarDraft() {
        ChecklistExecutionDraftCreateDTO request = new ChecklistExecutionDraftCreateDTO(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), ChecklistType.ARRIVAL);
        ChecklistExecution execution = mock(ChecklistExecution.class);
        ChecklistExecutionResponseDTO response = mock(ChecklistExecutionResponseDTO.class);

        when(createUseCase.execute(request)).thenReturn(execution);
        when(mapper.toResponse(execution)).thenReturn(response);

        ResponseEntity<ChecklistExecutionResponseDTO> result = controller.createDraft(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(createUseCase).execute(request);
        verify(mapper).toResponse(execution);
    }
}
