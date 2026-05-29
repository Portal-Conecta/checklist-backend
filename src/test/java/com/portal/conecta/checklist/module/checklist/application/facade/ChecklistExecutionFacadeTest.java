package com.portal.conecta.checklist.module.checklist.application.facade;

import com.portal.conecta.checklist.module.checklist.application.usecase.execution.CreateChecklistExecutionUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.ListChecklistHistoryByClassUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.SubmitChecklistExecutionUseCase;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.CancelChecklistExecutionUseCase;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChecklistExecutionFacadeTest {

    private final CreateChecklistExecutionUseCase createChecklistExecutionUseCase = mock(CreateChecklistExecutionUseCase.class);
    private final SubmitChecklistExecutionUseCase submitChecklistExecutionUseCase = mock(SubmitChecklistExecutionUseCase.class);
    private final ChecklistExecutionMapper executionMapper = mock(ChecklistExecutionMapper.class);
    private final ListChecklistHistoryByClassUseCase listChecklistHistoryByClassUseCase = mock(ListChecklistHistoryByClassUseCase.class);
    private final CancelChecklistExecutionUseCase cancelChecklistExecutionUseCase = mock(CancelChecklistExecutionUseCase.class); // ← essa linha estava faltando
    private final ChecklistExecutionFacade facade = new ChecklistExecutionFacade(
            createChecklistExecutionUseCase,
            submitChecklistExecutionUseCase,
            executionMapper,
            cancelChecklistExecutionUseCase,
            listChecklistHistoryByClassUseCase
    );

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

    @Test
    @DisplayName("deve enviar execucao e retornar dto mapeado")
    void deveEnviarExecucaoERetornarDtoMapeado() {
        UUID executionId = UUID.randomUUID();
        ChecklistExecutionSubmitDTO request = mock(ChecklistExecutionSubmitDTO.class);
        ChecklistExecution execution = ChecklistExecution.builder()
                .id(executionId)
                .build();
        ChecklistExecutionResponseDTO response = mock(ChecklistExecutionResponseDTO.class);

        when(submitChecklistExecutionUseCase.execute(executionId, request)).thenReturn(execution);
        when(executionMapper.toResponse(execution)).thenReturn(response);

        ChecklistExecutionResponseDTO result = facade.submit(executionId, request);

        assertSame(response, result);
        verify(submitChecklistExecutionUseCase).execute(executionId, request);
        verify(executionMapper).toResponse(execution);
    }
}
