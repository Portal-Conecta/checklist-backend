package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelChecklistExecutionUseCaseTest {

    @Mock
    private ChecklistExecutionRepository executionRepository;

    @InjectMocks
    private CancelChecklistExecutionUseCase cancelChecklistExecutionUseCase;

    @Test
    @DisplayName("deve cancelar execucao com status SUBMITTED com sucesso")
    void deveCancelarExecucaoComSucesso() {
        UUID executionId = UUID.randomUUID();
        ChecklistExecution execution = new ChecklistExecution();
        execution.setStatus(ChecklistExecutionStatus.SUBMITTED);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(executionRepository.save(execution)).thenReturn(execution);

        ChecklistExecution resultado = cancelChecklistExecutionUseCase.execute(executionId);

        assertEquals(ChecklistExecutionStatus.CANCELED, resultado.getStatus());
        verify(executionRepository, times(1)).findById(executionId);
        verify(executionRepository, times(1)).save(execution);
    }

    @Test
    @DisplayName("deve lancar EntityNotFoundException quando execucao nao existe")
    void deveLancarExcecaoQuandoExecucaoNaoExiste() {
        UUID executionId = UUID.randomUUID();
        when(executionRepository.findById(executionId)).thenReturn(Optional.empty());

        EntityNotFoundException excecao = assertThrows(EntityNotFoundException.class,
                () -> cancelChecklistExecutionUseCase.execute(executionId));

        assertEquals("Execucao de checklist nao encontrada", excecao.getMessage());
        verify(executionRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve lancar IllegalArgumentException quando status e DRAFT")
    void deveLancarExcecaoQuandoStatusEDraft() {
        UUID executionId = UUID.randomUUID();
        ChecklistExecution execution = new ChecklistExecution();
        execution.setStatus(ChecklistExecutionStatus.DRAFT);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class,
                () -> cancelChecklistExecutionUseCase.execute(executionId));

        assertEquals("Somente checklist enviados podem ser cancelados", excecao.getMessage());
        verify(executionRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve lancar IllegalArgumentException quando status ja e CANCELED")
    void deveLancarExcecaoQuandoStatusJaECanceled() {
        UUID executionId = UUID.randomUUID();
        ChecklistExecution execution = new ChecklistExecution();
        execution.setStatus(ChecklistExecutionStatus.CANCELED);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class,
                () -> cancelChecklistExecutionUseCase.execute(executionId));

        assertEquals("Somente checklist enviados podem ser cancelados", excecao.getMessage());
        verify(executionRepository, never()).save(any());
    }
}