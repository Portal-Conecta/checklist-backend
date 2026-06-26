package com.portal.conecta.checklist.module.checklist.application.facade;

import com.portal.conecta.checklist.module.checklist.application.usecase.execution.*;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class ChecklistExecutionFacadeTest {

    private final CreateChecklistExecutionUseCase createChecklistExecutionUseCase = mock(CreateChecklistExecutionUseCase.class);
    private final SubmitChecklistExecutionUseCase submitChecklistExecutionUseCase = mock(SubmitChecklistExecutionUseCase.class);
    private final CancelChecklistExecutionUseCase cancelChecklistExecutionUseCase = mock(CancelChecklistExecutionUseCase.class);
    private final ListChecklistExecutionUseCase listChecklistExecutionUseCase = mock(ListChecklistExecutionUseCase.class);
    private final FindChecklistExecutionByIdUseCase findChecklistExecutionByIdUseCase = mock(FindChecklistExecutionByIdUseCase.class);
    private final ChecklistExecutionMapper executionMapper = mock(ChecklistExecutionMapper.class);

    private final ChecklistExecutionFacade facade;

    ChecklistExecutionFacadeTest() {
        facade = new ChecklistExecutionFacade(
                createChecklistExecutionUseCase,
                submitChecklistExecutionUseCase,
                executionMapper,
                cancelChecklistExecutionUseCase,
                findChecklistExecutionByIdUseCase,
                listChecklistExecutionUseCase

        );
    }

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

        when(submitChecklistExecutionUseCase.execute(executionId, request))
                .thenReturn(execution);

        when(executionMapper.toResponse(execution))
                .thenReturn(response);

        ChecklistExecutionResponseDTO result =
                facade.submit(executionId, request);

        assertSame(response, result);

        verify(submitChecklistExecutionUseCase)
                .execute(executionId, request);

        verify(executionMapper)
                .toResponse(execution);
    }

    @Test
    @DisplayName("deve listar execucoes paginadas")
    void deveListarExecucoesPaginadas() {

        Pageable pageable = PageRequest.of(0, 10);

        ChecklistExecution execution = ChecklistExecution.builder()
                .id(UUID.randomUUID())
                .build();

        ChecklistExecutionResponseDTO response =
                mock(ChecklistExecutionResponseDTO.class);

        Page<ChecklistExecution> executionPage =
                new PageImpl<>(List.of(execution));

        when(listChecklistExecutionUseCase.execute(pageable))
                .thenReturn(executionPage);

        when(executionMapper.toResponse(execution))
                .thenReturn(response);

        Page<ChecklistExecutionResponseDTO> result =
                facade.listExecution(pageable);

        assertSame(response, result.getContent().getFirst());

        verify(listChecklistExecutionUseCase)
                .execute(pageable);

        verify(executionMapper)
                .toResponse(execution);
    }

    @Test
    @DisplayName("deve buscar execucao por id e retornar dto mapeado")
    void deveBuscarExecucaoPorIdERetornarDtoMapeado() {
        UUID executionId = UUID.randomUUID();

        ChecklistExecution execution = ChecklistExecution.builder()
                .id(executionId)
                .build();

        ChecklistExecutionResponseDTO response = mock(ChecklistExecutionResponseDTO.class);

        when(findChecklistExecutionByIdUseCase.execute(executionId))
                .thenReturn(execution);

        when(executionMapper.toResponse(execution))
                .thenReturn(response);

        ChecklistExecutionResponseDTO result = facade.findExecutionById(executionId);

        assertSame(response, result);

        verify(findChecklistExecutionByIdUseCase).execute(executionId);
        verify(executionMapper).toResponse(execution);
    }
}