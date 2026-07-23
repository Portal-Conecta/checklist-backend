package com.portal.conecta.checklist.unit.checklist.application.usecase.execution.command;

import com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.cancel.CancelChecklistExecutionUseCase;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.shared.context.ClassRole;
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

import static org.junit.jupiter.api.Assertions.*;
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
    @DisplayName("deve cancelar execucao com status SUBMITTED com sucesso e preencher canceledBy")
    void deveCancelarExecucaoComSucesso() {
        UUID executionId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID cancelerId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistExecution execution = submitted(creatorId, classId);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(representative(cancelerId, classId));
        when(executionRepository.save(execution)).thenReturn(execution);

        ChecklistExecution resultado = cancelChecklistExecutionUseCase.execute(executionId);

        assertEquals(ChecklistExecutionStatus.CANCELED, resultado.getStatus());
        assertEquals(cancelerId, resultado.getCanceledBy());
        assertNull(resultado.getSubmittedBy());
        verify(executionRepository).save(execution);
        verify(executionRepository, never()).countByUserIdAndStatus(any(), any());
    }

    @Test
    @DisplayName("representante B deve cancelar execucao criada pelo representante A da mesma turma")
    void devePermitirQueColegaRepresentanteCanceleExecucao() {
        UUID executionId = UUID.randomUUID();
        UUID representativeA = UUID.randomUUID();
        UUID representativeB = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistExecution execution = submitted(representativeA, classId);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(representative(representativeB, classId));
        when(executionRepository.save(execution)).thenReturn(execution);

        ChecklistExecution resultado = cancelChecklistExecutionUseCase.execute(executionId);

        assertEquals(ChecklistExecutionStatus.CANCELED, resultado.getStatus());
        assertEquals(representativeB, resultado.getCanceledBy());
        assertEquals(representativeA, resultado.getUserId());
    }

    @Test
    @DisplayName("representante com 2 SUBMITTED ativas consegue cancelar uma delas")
    void deveCancelarQuandoRepresentanteTemDuasExecucoesSubmittedAtivas() {
        UUID executionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistExecution execution = submitted(userId, classId);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(representative(userId, classId));
        when(executionRepository.save(execution)).thenReturn(execution);

        ChecklistExecution resultado = cancelChecklistExecutionUseCase.execute(executionId);

        assertEquals(ChecklistExecutionStatus.CANCELED, resultado.getStatus());
        assertEquals(userId, resultado.getCanceledBy());
        verify(executionRepository).save(execution);
    }

    @Test
    @DisplayName("cancelamento permanece permitido mesmo com 3 ou mais SUBMITTED ativas")
    void deveCancelarQuandoRepresentanteTemTresOuMaisSubmittedAtivas() {
        UUID executionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistExecution execution = submitted(userId, classId);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(representative(userId, classId));
        when(executionRepository.save(execution)).thenReturn(execution);

        ChecklistExecution resultado = cancelChecklistExecutionUseCase.execute(executionId);

        assertEquals(ChecklistExecutionStatus.CANCELED, resultado.getStatus());
        assertEquals(userId, resultado.getCanceledBy());
        verify(executionRepository).save(execution);
        verify(executionRepository, never()).countByUserIdAndStatus(any(), any());
    }

    @Test
    @DisplayName("deve permitir que gestor cancele execucao enviada e preencher canceledBy")
    void devePermitirQueGestorCanceleExecucaoEnviada() {
        UUID executionId = UUID.randomUUID();
        UUID gestorId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        ChecklistExecution execution = submitted(creatorId, UUID.randomUUID());

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(new RequestContext(gestorId, TypeUser.SENAI));
        when(executionRepository.save(execution)).thenReturn(execution);

        ChecklistExecution resultado = cancelChecklistExecutionUseCase.execute(executionId);

        assertEquals(ChecklistExecutionStatus.CANCELED, resultado.getStatus());
        assertEquals(gestorId, resultado.getCanceledBy());
        verify(executionRepository).save(execution);
    }

    @Test
    @DisplayName("deve negar cancelamento quando usuario nao tem vinculo operacional com a turma")
    void deveNegarCancelamentoQuandoUsuarioNaoTemVinculoOperacionalComATurma() {
        UUID executionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ChecklistExecution execution = submitted(userId, UUID.randomUUID());

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(representative(userId, UUID.randomUUID()));

        assertThrows(AccessDeniedException.class, () -> cancelChecklistExecutionUseCase.execute(executionId));

        verify(executionRepository, never()).save(any());
        assertNull(execution.getCanceledBy());
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
    @DisplayName("status DRAFT pode ser cancelado (libera o slot para recriar o checklist)")
    void deveCancelarQuandoStatusEDraft() {
        UUID executionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistExecution execution = new ChecklistExecution();
        execution.setUserId(userId);
        execution.setClassId(classId);
        execution.setStatus(ChecklistExecutionStatus.DRAFT);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(representative(userId, classId));
        when(executionRepository.save(execution)).thenReturn(execution);

        ChecklistExecution resultado = cancelChecklistExecutionUseCase.execute(executionId);

        assertEquals(ChecklistExecutionStatus.CANCELED, resultado.getStatus());
        assertEquals(userId, resultado.getCanceledBy());
        verify(executionRepository).save(execution);
    }

    @Test
    @DisplayName("usuario sem permissao recebe 403 antes da checagem de status — nao vaza estado da execucao")
    void deveNegarSemVazarEstadoQuandoUsuarioNaoTemPermissaoEStatusEDraft() {
        UUID executionId = UUID.randomUUID();
        ChecklistExecution execution = new ChecklistExecution();
        execution.setUserId(UUID.randomUUID());
        execution.setClassId(UUID.randomUUID());
        execution.setStatus(ChecklistExecutionStatus.DRAFT);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(representative(UUID.randomUUID(), UUID.randomUUID()));

        assertThrows(AccessDeniedException.class, () -> cancelChecklistExecutionUseCase.execute(executionId));

        verify(executionRepository, never()).save(any());
        assertNull(execution.getCanceledBy());
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

        assertEquals("Somente checklist em rascunho ou enviados podem ser cancelados", excecao.getMessage());
        verify(executionRepository, never()).save(any());
    }

    private ChecklistExecution submitted(UUID userId, UUID classId) {
        ChecklistExecution execution = new ChecklistExecution();
        execution.setUserId(userId);
        execution.setClassId(classId);
        execution.setStatus(ChecklistExecutionStatus.SUBMITTED);
        return execution;
    }

    private RequestContext representative(UUID userId, UUID classId) {
        return new RequestContext(
                userId,
                TypeUser.REPRESENTATIVE,
                List.of(new ContextClass(classId, ClassRole.REPRESENTATIVE))
        );
    }
}
