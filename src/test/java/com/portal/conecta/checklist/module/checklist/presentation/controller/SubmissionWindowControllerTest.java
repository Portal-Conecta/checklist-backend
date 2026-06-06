package com.portal.conecta.checklist.module.checklist.presentation.controller;

import com.portal.conecta.checklist.module.checklist.application.usecase.window.ListSubmissionWindowsUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.window.UpsertSubmissionWindowUseCase;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.module.checklist.domain.enums.Shift;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistSubmissionWindow;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.SubmissionWindowRequestDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.SubmissionWindowResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.SubmissionWindowMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SubmissionWindowControllerTest {

    private final UpsertSubmissionWindowUseCase upsertUseCase = mock(UpsertSubmissionWindowUseCase.class);
    private final ListSubmissionWindowsUseCase listUseCase = mock(ListSubmissionWindowsUseCase.class);
    private final SubmissionWindowMapper mapper = mock(SubmissionWindowMapper.class);
    private final SubmissionWindowController controller = new SubmissionWindowController(
            upsertUseCase,
            listUseCase,
            mapper
    );

    @Test
    @DisplayName("deve listar janelas de envio configuradas")
    void deveListarJanelasDeEnvioConfiguradas() {
        ChecklistSubmissionWindow window = mock(ChecklistSubmissionWindow.class);
        SubmissionWindowResponseDTO response = response();

        when(listUseCase.execute()).thenReturn(List.of(window));
        when(mapper.toResponseList(List.of(window))).thenReturn(List.of(response));

        ResponseEntity<List<SubmissionWindowResponseDTO>> result = controller.listAll();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(List.of(response), result.getBody());
        verify(listUseCase).execute();
        verify(mapper).toResponseList(List.of(window));
    }

    @Test
    @DisplayName("deve listar janelas de envio por turma")
    void deveListarJanelasDeEnvioPorTurma() {
        UUID classId = UUID.randomUUID();
        ChecklistSubmissionWindow window = mock(ChecklistSubmissionWindow.class);
        SubmissionWindowResponseDTO response = response();

        when(listUseCase.execute(classId)).thenReturn(List.of(window));
        when(mapper.toResponseList(List.of(window))).thenReturn(List.of(response));

        ResponseEntity<List<SubmissionWindowResponseDTO>> result = controller.listByClass(classId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(List.of(response), result.getBody());
        verify(listUseCase).execute(classId);
        verify(mapper).toResponseList(List.of(window));
    }

    @Test
    @DisplayName("deve configurar janela de envio por turma e tipo")
    void deveConfigurarJanelaDeEnvioPorTurmaETipo() {
        UUID classId = UUID.randomUUID();
        SubmissionWindowRequestDTO request = new SubmissionWindowRequestDTO(LocalTime.of(7, 30), 30);
        ChecklistSubmissionWindow window = mock(ChecklistSubmissionWindow.class);
        SubmissionWindowResponseDTO response = response();

        when(upsertUseCase.execute(classId, ChecklistType.ARRIVAL, request)).thenReturn(window);
        when(mapper.toResponse(window)).thenReturn(response);

        ResponseEntity<SubmissionWindowResponseDTO> result = controller.upsert(
                classId,
                ChecklistType.ARRIVAL,
                request
        );

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(upsertUseCase).execute(classId, ChecklistType.ARRIVAL, request);
        verify(mapper).toResponse(window);
    }

    private SubmissionWindowResponseDTO response() {
        return new SubmissionWindowResponseDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Shift.FULL_AM_PM,
                ChecklistType.ARRIVAL,
                LocalTime.of(7, 30),
                30,
                null,
                null
        );
    }
}
