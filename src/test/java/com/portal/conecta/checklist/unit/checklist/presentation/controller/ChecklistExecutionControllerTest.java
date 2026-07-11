package com.portal.conecta.checklist.unit.checklist.presentation.controller;

import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.cancel.CancelChecklistExecutionUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.create.CreateChecklistExecutionCommand;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.create.CreateChecklistExecutionUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.query.FindChecklistExecutionByIdUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.query.ListChecklistHistoryByClassUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.query.ListChecklistExecutionsUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.submit.SubmitChecklistExecutionUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.submit.SubmitChecklistExecutionCommand;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.update.UpdateChecklistExecutionAnswersUseCase;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.modules.checklist.presentation.controller.ChecklistExecutionController;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.execution.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.execution.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.execution.response.ChecklistExecutionHistoryDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.execution.response.ChecklistExecutionResponseDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.mapper.ChecklistExecutionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
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
    private final ListChecklistHistoryByClassUseCase listHistoryByClassUseCase = mock(ListChecklistHistoryByClassUseCase.class);
    private final ListChecklistExecutionsUseCase listExecutionsUseCase = mock(ListChecklistExecutionsUseCase.class);
    private final UpdateChecklistExecutionAnswersUseCase updateAnswersUseCase = mock(UpdateChecklistExecutionAnswersUseCase.class);
    private final FindChecklistExecutionByIdUseCase findByIdUseCase = mock(FindChecklistExecutionByIdUseCase.class);
    private final ChecklistExecutionMapper mapper = mock(ChecklistExecutionMapper.class);
    private final ChecklistExecutionController controller = new ChecklistExecutionController(
            createUseCase,
            submitUseCase,
            cancelUseCase,
            listHistoryByClassUseCase,
            listExecutionsUseCase,
            updateAnswersUseCase,
            findByIdUseCase,
            mapper
    );

    @Test
    @DisplayName("deve retornar created ao criar draft")
    void deveRetornarCreatedAoCriarDraft() {
        ChecklistExecutionDraftCreateDTO request = new ChecklistExecutionDraftCreateDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                ChecklistType.ARRIVAL
        );
        ChecklistExecution execution = mock(ChecklistExecution.class);
        ChecklistExecutionResponseDTO response = mock(ChecklistExecutionResponseDTO.class);
        CreateChecklistExecutionCommand command = request.toCommand();

        when(createUseCase.execute(command)).thenReturn(execution);
        when(mapper.toResponse(execution)).thenReturn(response);

        ResponseEntity<ChecklistExecutionResponseDTO> result = controller.createDraft(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(createUseCase).execute(command);
        verify(mapper).toResponse(execution);
    }

    @Test
    @DisplayName("deve retornar ok ao enviar checklist")
    void deveRetornarOkAoEnviarChecklist() {
        UUID executionId = UUID.randomUUID();
        ChecklistExecutionSubmitDTO request = mock(ChecklistExecutionSubmitDTO.class);
        SubmitChecklistExecutionCommand command = mock(SubmitChecklistExecutionCommand.class);
        ChecklistExecution execution = mock(ChecklistExecution.class);
        ChecklistExecutionResponseDTO response = mock(ChecklistExecutionResponseDTO.class);

        when(request.toCommand()).thenReturn(command);
        when(submitUseCase.execute(executionId, command)).thenReturn(execution);
        when(mapper.toResponse(execution)).thenReturn(response);

        ResponseEntity<ChecklistExecutionResponseDTO> result = controller.submit(executionId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(submitUseCase).execute(executionId, command);
        verify(mapper).toResponse(execution);
    }

    @Test
    @DisplayName("deve retornar ok ao cancelar checklist")
    void deveRetornarOkAoCancelarChecklist() {
        UUID executionId = UUID.randomUUID();
        ChecklistExecution execution = mock(ChecklistExecution.class);
        ChecklistExecutionResponseDTO response = mock(ChecklistExecutionResponseDTO.class);

        when(cancelUseCase.execute(executionId)).thenReturn(execution);
        when(mapper.toResponse(execution)).thenReturn(response);

        ResponseEntity<ChecklistExecutionResponseDTO> result = controller.cancel(executionId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(cancelUseCase).execute(executionId);
        verify(mapper).toResponse(execution);
    }

    @Test
    @DisplayName("deve retornar ok ao atualizar respostas de checklist enviado")
    void deveRetornarOkAoAtualizarRespostas() {
        UUID executionId = UUID.randomUUID();
        ChecklistExecutionSubmitDTO request = mock(ChecklistExecutionSubmitDTO.class);
        SubmitChecklistExecutionCommand command = mock(SubmitChecklistExecutionCommand.class);
        ChecklistExecution execution = mock(ChecklistExecution.class);
        ChecklistExecutionResponseDTO response = mock(ChecklistExecutionResponseDTO.class);

        when(request.toCommand()).thenReturn(command);
        when(updateAnswersUseCase.execute(executionId, command)).thenReturn(execution);
        when(mapper.toResponse(execution)).thenReturn(response);

        ResponseEntity<ChecklistExecutionResponseDTO> result = controller.updateAnswers(executionId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(updateAnswersUseCase).execute(executionId, command);
        verify(mapper).toResponse(execution);
    }

    @Test
    @DisplayName("deve retornar historico paginado por turma")
    void deveRetornarHistoricoPaginadoPorTurma() {
        UUID classId = UUID.randomUUID();
        PageRequest pageable = PageRequest.of(0, 20);
        ChecklistExecution execution = mock(ChecklistExecution.class);
        ChecklistExecutionHistoryDTO history = mock(ChecklistExecutionHistoryDTO.class);
        Page<ChecklistExecution> executions = new PageImpl<>(List.of(execution), pageable, 1);
        Page<ChecklistExecutionHistoryDTO> response = new PageImpl<>(List.of(history), pageable, 1);

        when(listHistoryByClassUseCase.execute(classId, pageable)).thenReturn(executions);
        when(mapper.toPageHistoryWithEnrichment(executions, classId)).thenReturn(response);

        ResponseEntity<Page<ChecklistExecutionHistoryDTO>> result = controller.listHistoryByClass(classId, pageable);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(listHistoryByClassUseCase).execute(classId, pageable);
        verify(mapper).toPageHistoryWithEnrichment(executions, classId);
    }

    @Test
    @DisplayName("deve retornar ok ao buscar por ID")
    void deveRetornarOkAoBuscarPorId() {
        UUID executionId = UUID.randomUUID();
        ChecklistExecution execution = mock(ChecklistExecution.class);
        ChecklistExecutionResponseDTO response = mock(ChecklistExecutionResponseDTO.class);

        when(findByIdUseCase.execute(executionId)).thenReturn(execution);
        when(mapper.toResponse(execution)).thenReturn(response);

        ResponseEntity<ChecklistExecutionResponseDTO> result = controller.findById(executionId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(findByIdUseCase).execute(executionId);
        verify(mapper).toResponse(execution);
    }
}
