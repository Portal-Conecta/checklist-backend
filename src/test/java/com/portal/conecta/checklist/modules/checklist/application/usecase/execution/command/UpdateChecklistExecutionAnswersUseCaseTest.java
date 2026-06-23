package com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.modules.checklist.application.service.execution.ChecklistExecutionAnswerValidationService;
import com.portal.conecta.checklist.modules.checklist.application.service.execution.ChecklistExecutionScoringService;
import com.portal.conecta.checklist.modules.checklist.application.service.execution.ChecklistIssueService;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.update.UpdateChecklistExecutionAnswersUseCase;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.submit.SubmitChecklistExecutionCommand;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSchema;
import com.portal.conecta.checklist.modules.checklist.application.service.execution.ChecklistExecutionDataMapper;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateChecklistExecutionAnswersUseCaseTest {

    @Mock
    private ChecklistExecutionRepository executionRepository;

    @Mock
    private RequestContextProvider contextProvider;

    @Mock
    private ChecklistExecutionDataMapper executionMapper;

    @Mock
    private ChecklistExecutionScoringService scoringService;

    @Mock
    private ChecklistIssueService issueService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ChecklistExecutionAnswerValidationService answerValidationService;

    @InjectMocks
    private UpdateChecklistExecutionAnswersUseCase updateChecklistExecutionAnswersUseCase;

    @Test
    @DisplayName("deve atualizar respostas com sucesso quando usuario for professor da turma")
    void deveAtualizarRespostasComSucesso() {
        UUID executionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();

        SubmitChecklistExecutionCommand request = mock(SubmitChecklistExecutionCommand.class);
        when(request.answers()).thenReturn(List.of());

        ChecklistExecution execution = new ChecklistExecution();
        execution.setUserId(userId);
        execution.setClassId(classId);
        execution.setStatus(ChecklistExecutionStatus.SUBMITTED);

        ChecklistTemplate template = mock(ChecklistTemplate.class);
        execution.setChecklistTemplate(template);

        ChecklistSchema schema = mock(ChecklistSchema.class);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(teacherContext(userId, classId));
        when(objectMapper.convertValue(any(), eq(ChecklistSchema.class))).thenReturn(schema);
        when(answerValidationService.validate(schema, request.answers())).thenReturn(Map.of());
        when(executionRepository.save(execution)).thenReturn(execution);

        ChecklistExecution resultado = updateChecklistExecutionAnswersUseCase.execute(executionId, request);

        assertNotNull(resultado);
        verify(executionRepository, times(1)).findById(executionId);
        verify(objectMapper, times(1)).convertValue(any(), eq(ChecklistSchema.class));
        verify(answerValidationService, times(1)).validate(schema, request.answers());
        verify(scoringService, times(1)).calculateComplianceScore(any());
        verify(executionMapper, times(1)).toAnswersJson(request);
        verify(issueService, times(1)).createIssuesForNonCompliantAnswers(any(), any(), any());
        verify(executionRepository, times(1)).save(execution);
    }

    @Test
    @DisplayName("deve rejeitar atualizacao quando respostas violarem o schema")
    void deveRejeitarAtualizacaoQuandoRespostasForemInvalidas() {
        UUID executionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();

        SubmitChecklistExecutionCommand request = mock(SubmitChecklistExecutionCommand.class);
        when(request.answers()).thenReturn(List.of());

        ChecklistExecution execution = new ChecklistExecution();
        execution.setUserId(userId);
        execution.setClassId(classId);
        execution.setStatus(ChecklistExecutionStatus.SUBMITTED);

        ChecklistTemplate template = mock(ChecklistTemplate.class);
        execution.setChecklistTemplate(template);

        ChecklistSchema schema = mock(ChecklistSchema.class);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(teacherContext(userId, classId));
        when(objectMapper.convertValue(any(), eq(ChecklistSchema.class))).thenReturn(schema);
        when(answerValidationService.validate(schema, request.answers()))
                .thenThrow(new IllegalArgumentException("Item obrigatorio sem resposta: quadro"));

        assertThrows(IllegalArgumentException.class,
                () -> updateChecklistExecutionAnswersUseCase.execute(executionId, request));

        verify(answerValidationService, times(1)).validate(schema, request.answers());
        verify(issueService, never()).createIssuesForNonCompliantAnswers(any(), any(), any());
        verify(executionRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve lancar EntityNotFoundException quando execucao nao existir para atualizacao")
    void deveLancarExcecaoQuandoExecucaoNaoExiste() {
        UUID executionId = UUID.randomUUID();
        SubmitChecklistExecutionCommand request = mock(SubmitChecklistExecutionCommand.class);

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

        SubmitChecklistExecutionCommand request = mock(SubmitChecklistExecutionCommand.class);
        ChecklistExecution execution = new ChecklistExecution();
        execution.setUserId(UUID.randomUUID());
        execution.setClassId(classId);
        execution.setStatus(ChecklistExecutionStatus.SUBMITTED);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        RequestContext contextComRoleInvalida = new RequestContext(
                UUID.randomUUID(),
                TypeUser.REPRESENTATIVE,
                List.of(new ContextClass(classId, ClassRole.STUDENT))
        );
        when(contextProvider.getRequestContext()).thenReturn(contextComRoleInvalida);

        assertThrows(AccessDeniedException.class,
                () -> updateChecklistExecutionAnswersUseCase.execute(executionId, request));

        verify(executionRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve rejeitar atualizacao quando execucao nao estiver no status SUBMITTED")
    void deveRejeitarAtualizacaoQuandoStatusNaoForSubmitted() {
        UUID executionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();

        SubmitChecklistExecutionCommand request = mock(SubmitChecklistExecutionCommand.class);
        ChecklistExecution execution = new ChecklistExecution();
        execution.setUserId(userId);
        execution.setClassId(classId);
        execution.setStatus(ChecklistExecutionStatus.DRAFT);

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(teacherContext(userId, classId));

        assertThrows(IllegalStateException.class,
                () -> updateChecklistExecutionAnswersUseCase.execute(executionId, request));

        verify(executionRepository, never()).save(any());
    }

    private RequestContext teacherContext(UUID userId, UUID classId) {
        return new RequestContext(
                userId,
                TypeUser.TEACHER,
                List.of(new ContextClass(classId, ClassRole.TEACHER))
        );
    }
}
