package com.portal.conecta.checklist.module.checklist.presentation.controller;

import com.portal.conecta.checklist.module.checklist.application.facade.ChecklistExecutionFacade;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.query.ListChecklistHistoryByClassUseCase;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.module.checklist.domain.enums.Period;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChecklistExecutionControllerTest {

    private final ChecklistExecutionFacade checklistExecutionFacade = mock(ChecklistExecutionFacade.class);
    private final ChecklistExecutionMapper checklistExecutionMapper = mock(ChecklistExecutionMapper.class);
    private final ListChecklistHistoryByClassUseCase listHistoryByClassUseCase = mock(ListChecklistHistoryByClassUseCase.class);

    private final ChecklistExecutionController controller = new ChecklistExecutionController(
            checklistExecutionFacade,
            checklistExecutionMapper,
            listHistoryByClassUseCase
    );

    @Test
    @DisplayName("deve retornar created ao criar draft")
    void deveRetornarCreatedAoCriarDraft() {
        ChecklistExecutionDraftCreateDTO request = new ChecklistExecutionDraftCreateDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Period.MORNING,
                ChecklistType.ARRIVAL
        );
        ChecklistExecutionResponseDTO response = mock(ChecklistExecutionResponseDTO.class);

        when(checklistExecutionFacade.createDTO(request)).thenReturn(response);

        ResponseEntity<ChecklistExecutionResponseDTO> result = controller.createDraft(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(checklistExecutionFacade).createDTO(request);
    }

    @Test
    @DisplayName("deve retornar ok ao enviar checklist")
    void deveRetornarOkAoEnviarChecklist() {
        UUID executionId = UUID.randomUUID();
        ChecklistExecutionSubmitDTO request = mock(ChecklistExecutionSubmitDTO.class);
        ChecklistExecutionResponseDTO response = mock(ChecklistExecutionResponseDTO.class);

        when(checklistExecutionFacade.submit(executionId, request)).thenReturn(response);

        ResponseEntity<ChecklistExecutionResponseDTO> result = controller.submit(executionId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(checklistExecutionFacade).submit(executionId, request);
    }

    @Test
    @DisplayName("deve retornar 200 OK ao cancelar execucao com sucesso")
    void deveRetornarOkAoCancelarExecucao() {
        UUID executionId = UUID.randomUUID();
        ChecklistExecutionResponseDTO response = mock(ChecklistExecutionResponseDTO.class);

        when(checklistExecutionFacade.cancel(executionId)).thenReturn(response);

        ResponseEntity<ChecklistExecutionResponseDTO> result = controller.cancel(executionId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(checklistExecutionFacade).cancel(executionId);
    }

    @Test
    @DisplayName("deve propagar EntityNotFoundException quando execucao nao existe")
    void devePropagar404QuandoExecucaoNaoExiste() {
        UUID executionId = UUID.randomUUID();

        when(checklistExecutionFacade.cancel(executionId))
                .thenThrow(new EntityNotFoundException("Execucao de checklist nao encontrada."));

        EntityNotFoundException excecao = assertThrows(EntityNotFoundException.class,
                () -> controller.cancel(executionId));

        assertEquals("Execucao de checklist nao encontrada.", excecao.getMessage());
        verify(checklistExecutionFacade).cancel(executionId);
    }

    @Test
    @DisplayName("deve propagar IllegalStateException quando status nao permite cancelamento")
    void devePropagar400QuandoStatusInvalido() {
        UUID executionId = UUID.randomUUID();

        when(checklistExecutionFacade.cancel(executionId))
                .thenThrow(new IllegalStateException("Somente checklists enviados podem ser cancelados."));

        IllegalStateException excecao = assertThrows(IllegalStateException.class,
                () -> controller.cancel(executionId));

        assertEquals("Somente checklists enviados podem ser cancelados.", excecao.getMessage());
        verify(checklistExecutionFacade).cancel(executionId);
    }

    @Test
    @DisplayName("deve retornar ok ao atualizar respostas do checklist via patch")
    void deveRetornarOkAoAtualizarRespostas() {
        UUID executionId = UUID.randomUUID();
        ChecklistExecutionSubmitDTO request = mock(ChecklistExecutionSubmitDTO.class);
        ChecklistExecutionResponseDTO response = mock(ChecklistExecutionResponseDTO.class);

        when(checklistExecutionFacade.updateAnswers(executionId, request)).thenReturn(response);

        ResponseEntity<ChecklistExecutionResponseDTO> result = controller.updateAnswers(executionId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(checklistExecutionFacade).updateAnswers(executionId, request);
    }
}