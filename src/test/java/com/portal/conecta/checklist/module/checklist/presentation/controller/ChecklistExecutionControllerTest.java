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

    private final ChecklistExecutionFacade facade = mock(ChecklistExecutionFacade.class);
    private final ChecklistExecutionMapper mapper = mock(ChecklistExecutionMapper.class);
    private final ListChecklistHistoryByClassUseCase listHistoryByClassUseCase = mock(ListChecklistHistoryByClassUseCase.class);
    private final ChecklistExecutionController controller = new ChecklistExecutionController(
            facade,
            mapper,
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

        when(facade.createDTO(request)).thenReturn(response);

        ResponseEntity<ChecklistExecutionResponseDTO> result = controller.createDraft(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(facade).createDTO(request);
    }

    @Test
    @DisplayName("deve retornar ok ao enviar checklist")
    void deveRetornarOkAoEnviarChecklist() {
        UUID executionId = UUID.randomUUID();
        ChecklistExecutionSubmitDTO request = mock(ChecklistExecutionSubmitDTO.class);
        ChecklistExecutionResponseDTO response = mock(ChecklistExecutionResponseDTO.class);

        when(facade.submit(executionId, request)).thenReturn(response);

        ResponseEntity<ChecklistExecutionResponseDTO> result = controller.submit(executionId, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(facade).submit(executionId, request);
    }

    // -----------------------------------------------
    // Testes do endpoint cancel
    // -----------------------------------------------

    @Test
    @DisplayName("deve retornar 200 OK ao cancelar execucao com sucesso")
    void deveRetornarOkAoCancelarExecucao() {
        UUID executionId = UUID.randomUUID();
        ChecklistExecutionResponseDTO response = mock(ChecklistExecutionResponseDTO.class);

        when(facade.cancel(executionId)).thenReturn(response);

        ResponseEntity<ChecklistExecutionResponseDTO> result = controller.cancel(executionId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(facade).cancel(executionId);
    }

    @Test
    @DisplayName("deve propagar EntityNotFoundException quando execucao nao existe")
    void devePropagar404QuandoExecucaoNaoExiste() {
        UUID executionId = UUID.randomUUID();

        when(facade.cancel(executionId))
                .thenThrow(new EntityNotFoundException("Execucao de checklist nao encontrada."));

        EntityNotFoundException excecao = assertThrows(EntityNotFoundException.class,
                () -> controller.cancel(executionId));

        assertEquals("Execucao de checklist nao encontrada.", excecao.getMessage());
        verify(facade).cancel(executionId);
    }

    @Test
    @DisplayName("deve propagar IllegalStateException quando status nao permite cancelamento")
    void devePropagar400QuandoStatusInvalido() {
        UUID executionId = UUID.randomUUID();

        when(facade.cancel(executionId))
                .thenThrow(new IllegalStateException("Somente checklists enviados podem ser cancelados."));

        IllegalStateException excecao = assertThrows(IllegalStateException.class,
                () -> controller.cancel(executionId));

        assertEquals("Somente checklists enviados podem ser cancelados.", excecao.getMessage());
        verify(facade).cancel(executionId);
    }
}
