package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.core.ChecklistExecutionScoringService;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.update.UpdateChecklistExecutionAnswersUseCase;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
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
class UpdateChecklistExecutionAnswersUseCaseTest {

    @Mock
    private ChecklistExecutionRepository executionRepository;

    @Mock
    private RequestContextProvider contextProvider;

    @Mock
    private ChecklistExecutionMapper executionMapper;

    @Mock
    private ChecklistExecutionScoringService scoringService;

    @InjectMocks
    private UpdateChecklistExecutionAnswersUseCase updateChecklistExecutionAnswersUseCase;

    @Test
    @DisplayName("deve atualizar respostas com sucesso quando usuario for professor da turma")
    void deveAtualizarRespostasComSucesso() {
        UUID executionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();

        ChecklistExecutionSubmitDTO request = mock(ChecklistExecutionSubmitDTO.class);
        ChecklistExecution execution = new ChecklistExecution();
        execution.setUserId(userId);
        execution.setClassId(classId);
        execution.setStatus(ChecklistExecutionStatus.SUBMITTED);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(teacherContext(userId, classId));
        when(executionRepository.save(execution)).thenReturn(execution);

        ChecklistExecution resultado = updateChecklistExecutionAnswersUseCase.execute(executionId, request);

        assertNotNull(resultado);
        verify(executionRepository, times(1)).findById(executionId);
        verify(scoringService, times(1)).calculateComplianceScore(any());
        verify(executionMapper, times(1)).toAnswersJson(request);
        verify(executionRepository, times(1)).save(execution);
    }

    @Test
    @DisplayName("deve lancar EntityNotFoundException quando execucao nao existir para atualizacao")
    void deveLancarExcecaoQuandoExecucaoNaoExiste() {
        UUID executionId = UUID.randomUUID();
        ChecklistExecutionSubmitDTO request = mock(ChecklistExecutionSubmitDTO.class);

        when(executionRepository.findById(executionId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> updateChecklistExecutionAnswersUseCase.execute(executionId, request));

        verify(executionRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve negar atualizacao se usuario nao tiver a role TEACHER na turma informada")
    void deveNegarAtualizacaoQuandoUsuarioNaoForProfessor() {
        UUID executionId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();

        ChecklistExecutionSubmitDTO request = mock(ChecklistExecutionSubmitDTO.class);
        ChecklistExecution execution = new ChecklistExecution();
        execution.setUserId(UUID.randomUUID());
        execution.setClassId(classId);
        execution.setStatus(ChecklistExecutionStatus.SUBMITTED);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        RequestContext contextComRoleInvalida = new RequestContext(
                UUID.randomUUID(),
                TypeUser.REPRESENTATIVE,
                List.of(new ContextClass(classId, "STUDENT"))
        );
        when(contextProvider.getRequestContext()).thenReturn(contextComRoleInvalida);

        assertThrows(AccessDeniedException.class,
                () -> updateChecklistExecutionAnswersUseCase.execute(executionId, request));

        verify(executionRepository, never()).save(any());
    }

    private RequestContext teacherContext(UUID userId, UUID classId) {
        return new RequestContext(
                userId,
                TypeUser.REPRESENTATIVE,
                List.of(new ContextClass(classId, "TEACHER"))
        );
    }
}