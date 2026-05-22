package com.portal.conecta.checklist.module.checklist.application.facade;

import com.portal.conecta.checklist.module.checklist.application.usecase.execution.CreateChecklistExecutionUseCase;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChecklistExecutionFacadeTest {

    private final CreateChecklistExecutionUseCase createChecklistExecutionUseCase = mock(CreateChecklistExecutionUseCase.class);
    private final ChecklistExecutionMapper executionMapper = mock(ChecklistExecutionMapper.class);
    private final ChecklistExecutionFacade facade = new ChecklistExecutionFacade(createChecklistExecutionUseCase, executionMapper);

    @Test
    @DisplayName("deve criar draft e retornar dto mapeado")
    void deveCriarDraftERetornarDtoMapeado() {
        ChecklistExecutionDraftCreateDTO request = mock(ChecklistExecutionDraftCreateDTO.class);
        ChecklistExecution execution = ChecklistExecution.builder()
                .id(UUID.randomUUID())
                .build();
        ChecklistExecutionResponseDTO response = mock(ChecklistExecutionResponseDTO.class);

        when(createChecklistExecutionUseCase.execute(request)).thenReturn(execution);
        when(executionMapper.toResponse(execution)).thenReturn(response);

        ChecklistExecutionResponseDTO result = facade.createDTO(request);

        assertSame(response, result);
        verify(createChecklistExecutionUseCase).execute(request);
        verify(executionMapper).toResponse(execution);
    }
}
