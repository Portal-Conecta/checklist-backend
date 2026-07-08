package com.portal.conecta.checklist.modules.checklist.issues.presentation.controller;

import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command.*;
import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command.CancelIssueUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command.reopen.ReopenIssueUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command.resolved.ResolveIssueUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command.start.RestartProgressIssueUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command.start.StartIssueProgressUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command.validate.ValidateIssueUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.query.ListIssuesByExecutionUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.domain.model.ChecklistIssue;
import com.portal.conecta.checklist.modules.checklist.issues.presentation.dto.response.ChecklistIssueResponseDTO;
import com.portal.conecta.checklist.modules.checklist.issues.presentation.mapper.ChecklistIssueMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class ChecklistIssueControllerTest {

    private final ListIssuesByExecutionUseCase listUseCase = mock(ListIssuesByExecutionUseCase.class);
    private final ResolveIssueUseCase resolveUseCase = mock(ResolveIssueUseCase.class);
    private final StartIssueProgressUseCase startUseCase = mock(StartIssueProgressUseCase.class);
    private final ValidateIssueUseCase validateUseCase = mock(ValidateIssueUseCase.class);
    private final ReopenIssueUseCase reopenUseCase = mock(ReopenIssueUseCase.class);
    private final RestartProgressIssueUseCase restartProgressUseCase = mock(RestartProgressIssueUseCase.class);
    private final CancelIssueUseCase cancelUseCase = mock(CancelIssueUseCase.class);
    private final ChecklistIssueMapper mapper = mock(ChecklistIssueMapper.class);

    private final ChecklistIssueController controller = new ChecklistIssueController(
            listUseCase,
            resolveUseCase,
            startUseCase,
            validateUseCase,
            reopenUseCase,
            restartProgressUseCase,
            cancelUseCase,
            mapper
    );

    @Test
    @DisplayName("deve listar issues por execucao")
    void deveListarIssuesPorExecucao() {
        UUID executionId = UUID.randomUUID();
        ChecklistIssue issue = mock(ChecklistIssue.class);
        ChecklistIssueResponseDTO response = mock(ChecklistIssueResponseDTO.class);

        when(listUseCase.execute(executionId)).thenReturn(List.of(issue));
        when(mapper.toResponseList(anyList())).thenReturn(List.of(response));

        ResponseEntity<List<ChecklistIssueResponseDTO>> result = controller.listByExecution(executionId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertSame(response, result.getBody().get(0));
        verify(listUseCase).execute(executionId);
    }

    @Test
    @DisplayName("deve iniciar progresso da issue")
    void deveIniciarProgressoDaIssue() {
        UUID issueId = UUID.randomUUID();
        ChecklistIssue issue = mock(ChecklistIssue.class);
        ChecklistIssueResponseDTO response = mock(ChecklistIssueResponseDTO.class);

        when(startUseCase.execute(issueId)).thenReturn(issue);
        when(mapper.toResponse(issue)).thenReturn(response);

        ResponseEntity<ChecklistIssueResponseDTO> result = controller.start(issueId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(startUseCase).execute(issueId);
    }

    @Test
    @DisplayName("deve resolver issue")
    void deveResolverIssue() {
        UUID issueId = UUID.randomUUID();
        ChecklistIssue issue = mock(ChecklistIssue.class);
        ChecklistIssueResponseDTO response = mock(ChecklistIssueResponseDTO.class);

        when(resolveUseCase.execute(issueId)).thenReturn(issue);
        when(mapper.toResponse(issue)).thenReturn(response);

        ResponseEntity<ChecklistIssueResponseDTO> result = controller.resolve(issueId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(resolveUseCase).execute(issueId);
    }

    @Test
    @DisplayName("deve validar issue")
    void deveValidarIssue() {
        UUID issueId = UUID.randomUUID();
        ChecklistIssue issue = mock(ChecklistIssue.class);
        ChecklistIssueResponseDTO response = mock(ChecklistIssueResponseDTO.class);

        when(validateUseCase.execute(issueId)).thenReturn(issue);
        when(mapper.toResponse(issue)).thenReturn(response);

        ResponseEntity<ChecklistIssueResponseDTO> result = controller.validate(issueId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(validateUseCase).execute(issueId);
    }

    @Test
    @DisplayName("deve reabrir issue")
    void deveReabrirIssue() {
        UUID issueId = UUID.randomUUID();
        ChecklistIssue issue = mock(ChecklistIssue.class);
        ChecklistIssueResponseDTO response = mock(ChecklistIssueResponseDTO.class);

        when(reopenUseCase.execute(issueId)).thenReturn(issue);
        when(mapper.toResponse(issue)).thenReturn(response);

        ResponseEntity<ChecklistIssueResponseDTO> result = controller.reopen(issueId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(reopenUseCase).execute(issueId);
    }

    @Test
    @DisplayName("deve retomar issue")
    void deveRetomarIssue() {
        UUID issueId = UUID.randomUUID();
        ChecklistIssue issue = mock(ChecklistIssue.class);
        ChecklistIssueResponseDTO response = mock(ChecklistIssueResponseDTO.class);

        when(restartProgressUseCase.execute(issueId)).thenReturn(issue);
        when(mapper.toResponse(issue)).thenReturn(response);

        ResponseEntity<ChecklistIssueResponseDTO> result = controller.restartProgress(issueId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(restartProgressUseCase).execute(issueId);
    }

    @Test
    @DisplayName("deve cancelar issue")
    void deveCancelarIssue() {
        UUID issueId = UUID.randomUUID();
        ChecklistIssue issue = mock(ChecklistIssue.class);
        ChecklistIssueResponseDTO response = mock(ChecklistIssueResponseDTO.class);

        when(cancelUseCase.execute(issueId)).thenReturn(issue);
        when(mapper.toResponse(issue)).thenReturn(response);

        ResponseEntity<ChecklistIssueResponseDTO> result = controller.cancel(issueId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(cancelUseCase).execute(issueId);
    }
}
