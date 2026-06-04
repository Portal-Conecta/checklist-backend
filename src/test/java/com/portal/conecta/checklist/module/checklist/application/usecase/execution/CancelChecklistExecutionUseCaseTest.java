package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.shared.context.ContextClass;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.context.TypeUser;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelChecklistExecutionUseCaseTest {

    @Mock
    private ChecklistExecutionRepository executionRepository;

    @Mock
    private RequestContextProvider contextProvider;

    @InjectMocks
    private CancelChecklistExecutionUseCase cancelChecklistExecutionUseCase;

    @Test
    @DisplayName("deve cancelar execucao com status SUBMITTED com sucesso")
    void deveCancelarExecucaoComSucesso() {
        UUID executionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistExecution execution = new ChecklistExecution();
        execution.setUserId(userId);
        execution.setClassId(classId);
        execution.setStatus(ChecklistExecutionStatus.SUBMITTED);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(representative(userId, classId));
        when(executionRepository.save(execution)).thenReturn(execution);

        ChecklistExecution resultado = cancelChecklistExecutionUseCase.execute(executionId);

        assertEquals(ChecklistExecutionStatus.CANCELED, resultado.getStatus());
        verify(executionRepository, times(1)).findById(executionId);
        verify(executionRepository, times(1)).save(execution);
    }

    @Test
    @DisplayName("deve permitir que gestor cancele execucao enviada")
    void devePermitirQueGestorCanceleExecucaoEnviada() {
        UUID executionId = UUID.randomUUID();
        ChecklistExecution execution = new ChecklistExecution();
        execution.setUserId(UUID.randomUUID());
        execution.setClassId(UUID.randomUUID());
        execution.setStatus(ChecklistExecutionStatus.SUBMITTED);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.SENAI));
        when(executionRepository.save(execution)).thenReturn(execution);

        ChecklistExecution resultado = cancelChecklistExecutionUseCase.execute(executionId);

        assertEquals(ChecklistExecutionStatus.CANCELED, resultado.getStatus());
        verify(executionRepository).save(execution);
    }

    @Test
    @DisplayName("deve negar cancelamento quando usuario nao e dono nem gestor")
    void deveNegarCancelamentoQuandoUsuarioNaoEDonoNemGestor() {
        UUID executionId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistExecution execution = new ChecklistExecution();
        execution.setUserId(UUID.randomUUID());
        execution.setClassId(classId);
        execution.setStatus(ChecklistExecutionStatus.SUBMITTED);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(representative(UUID.randomUUID(), classId));

        assertThrows(AccessDeniedException.class, () -> cancelChecklistExecutionUseCase.execute(executionId));

        verify(executionRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve negar cancelamento quando usuario nao tem vinculo operacional com a turma")
    void deveNegarCancelamentoQuandoUsuarioNaoTemVinculoOperacionalComATurma() {
        UUID executionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ChecklistExecution execution = new ChecklistExecution();
        execution.setUserId(userId);
        execution.setClassId(UUID.randomUUID());
        execution.setStatus(ChecklistExecutionStatus.SUBMITTED);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(representative(userId, UUID.randomUUID()));

        assertThrows(AccessDeniedException.class, () -> cancelChecklistExecutionUseCase.execute(executionId));

        verify(executionRepository, never()).save(any());
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
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistExecution execution = new ChecklistExecution();
        execution.setUserId(userId);
        execution.setClassId(classId);
        execution.setStatus(ChecklistExecutionStatus.DRAFT);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(representative(userId, classId));

        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class,
                () -> cancelChecklistExecutionUseCase.execute(executionId));

        assertEquals("Somente checklist enviados podem ser cancelados", excecao.getMessage());
        verify(executionRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve lancar IllegalArgumentException quando status ja e CANCELED")
    void deveLancarExcecaoQuandoStatusJaECanceled() {
        UUID executionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistExecution execution = new ChecklistExecution();
        execution.setUserId(userId);
        execution.setClassId(classId);
        execution.setStatus(ChecklistExecutionStatus.CANCELED);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(representative(userId, classId));

        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class,
                () -> cancelChecklistExecutionUseCase.execute(executionId));

        assertEquals("Somente checklist enviados podem ser cancelados", excecao.getMessage());
        verify(executionRepository, never()).save(any());
    }

    private RequestContext representative(UUID userId, UUID classId) {
        return new RequestContext(
                userId,
                TypeUser.REPRESENTATIVE,
                List.of(new ContextClass(classId, "REPRESENTATIVE"))
        );
    }
}