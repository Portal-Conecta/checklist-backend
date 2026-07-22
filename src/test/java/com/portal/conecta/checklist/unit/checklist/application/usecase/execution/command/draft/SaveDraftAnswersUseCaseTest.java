package com.portal.conecta.checklist.unit.checklist.application.usecase.execution.command.draft;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.application.service.execution.ChecklistExecutionAnswerValidationService;
import com.portal.conecta.checklist.module.checklist.application.service.execution.ChecklistExecutionDataMapper;
import com.portal.conecta.checklist.module.checklist.application.service.execution.ChecklistExecutionScoringService;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.draft.SaveDraftAnswersUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.submit.SubmitChecklistExecutionCommand;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.update.UpdateChecklistAnswerCommand;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.module.checklist.domain.enums.ConformityAnswerValue;
import com.portal.conecta.checklist.module.checklist.domain.enums.Shift;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.shared.context.ClassRole;
import com.portal.conecta.checklist.shared.context.ContextClass;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.context.TypeUser;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SaveDraftAnswersUseCaseTest {

    private final ChecklistExecutionRepository executionRepository = mock(ChecklistExecutionRepository.class);
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final ChecklistExecutionDataMapper executionMapper = new ChecklistExecutionDataMapper(objectMapper);

    private SaveDraftAnswersUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SaveDraftAnswersUseCase(
                executionRepository,
                executionMapper,
                objectMapper,
                contextProvider,
                new ChecklistExecutionScoringService(),
                new ChecklistExecutionAnswerValidationService()
        );
    }

    @Test
    void shouldSaveEmptyDraftAnswers() {
        UUID executionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistExecution execution = draftExecution(executionId, userId, classId);
        SubmitChecklistExecutionCommand request = new SubmitChecklistExecutionCommand(List.of());

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(currentUser(userId, classId));
        when(executionRepository.save(execution)).thenReturn(execution);

        ChecklistExecution result = useCase.execute(executionId, request);

        assertThat(result.getStatus()).isEqualTo(ChecklistExecutionStatus.DRAFT);
        assertThat(result.getComplianceScore()).isEqualByComparingTo(BigDecimal.ZERO.setScale(2));
        verify(executionRepository).save(execution);
    }

    @Test
    void shouldSavePartialDraftAnswersWithMissingRequiredItem() {
        UUID executionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistExecution execution = draftExecution(executionId, userId, classId);
        SubmitChecklistExecutionCommand request = new SubmitChecklistExecutionCommand(List.of(
                answer("quadro", ConformityAnswerValue.COMPLIANT, null)
        ));

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(currentUser(userId, classId));
        when(executionRepository.save(execution)).thenReturn(execution);

        ChecklistExecution result = useCase.execute(executionId, request);

        assertThat(result.getStatus()).isEqualTo(ChecklistExecutionStatus.DRAFT);
        assertThat(result.getComplianceScore()).isEqualByComparingTo(new BigDecimal("100.00"));
        verify(executionRepository).save(execution);
    }

    @Test
    void shouldSaveNonCompliantAnswerWithoutObservation() {
        UUID executionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistExecution execution = draftExecution(executionId, userId, classId);
        SubmitChecklistExecutionCommand request = new SubmitChecklistExecutionCommand(List.of(
                answer("quadro", ConformityAnswerValue.NON_COMPLIANT, null)
        ));

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(currentUser(userId, classId));
        when(executionRepository.save(execution)).thenReturn(execution);

        ChecklistExecution result = useCase.execute(executionId, request);

        assertThat(result.getStatus()).isEqualTo(ChecklistExecutionStatus.DRAFT);
        assertThat(result.getComplianceScore()).isEqualByComparingTo(BigDecimal.ZERO.setScale(2));
        verify(executionRepository).save(execution);
    }

    @Test
    void shouldRejectAnswerForUnknownItemKey() {
        UUID executionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistExecution execution = draftExecution(executionId, userId, classId);
        SubmitChecklistExecutionCommand request = new SubmitChecklistExecutionCommand(List.of(
                answer("item-inexistente", ConformityAnswerValue.COMPLIANT, null)
        ));

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(currentUser(userId, classId));

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(executionId, request));

        verify(executionRepository, never()).save(execution);
    }

    @Test
    void shouldRejectSavingDraftWhenExecutionIsNotDraft() {
        UUID executionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistExecution execution = draftExecution(executionId, userId, classId);
        execution.setStatus(ChecklistExecutionStatus.SUBMITTED);
        SubmitChecklistExecutionCommand request = new SubmitChecklistExecutionCommand(List.of());

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(currentUser(userId, classId));

        assertThrows(IllegalStateException.class, () -> useCase.execute(executionId, request));

        verify(executionRepository, never()).save(execution);
    }

    @Test
    void shouldRejectSavingDraftWhenUserHasNoClassLink() {
        UUID executionId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistExecution execution = draftExecution(executionId, UUID.randomUUID(), classId);
        SubmitChecklistExecutionCommand request = new SubmitChecklistExecutionCommand(List.of());

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(currentUser(UUID.randomUUID(), UUID.randomUUID()));

        assertThrows(AccessDeniedException.class, () -> useCase.execute(executionId, request));

        verify(executionRepository, never()).save(execution);
    }

    @Test
    void shouldThrowEntityNotFoundWhenExecutionDoesNotExist() {
        UUID executionId = UUID.randomUUID();
        SubmitChecklistExecutionCommand request = new SubmitChecklistExecutionCommand(List.of());

        when(executionRepository.findById(executionId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(executionId, request));

        verify(executionRepository, never()).save(any());
    }

    private ChecklistExecution draftExecution(UUID executionId, UUID userId, UUID classId) {
        return ChecklistExecution.builder()
                .id(executionId)
                .userId(userId)
                .classId(classId)
                .shift(Shift.FULL_AM_PM)
                .checklistType(ChecklistType.ARRIVAL)
                .status(ChecklistExecutionStatus.DRAFT)
                .checklistTemplate(ChecklistTemplate.builder()
                        .id(UUID.randomUUID())
                        .version(1)
                        .schemaJson(schemaJson())
                        .build())
                .build();
    }

    private RequestContext currentUser(UUID userId, UUID classId) {
        return new RequestContext(
                userId,
                TypeUser.REPRESENTATIVE,
                List.of(new ContextClass(classId, ClassRole.REPRESENTATIVE))
        );
    }

    private Map<String, Object> schemaJson() {
        return Map.of(
                "sections", List.of(Map.of(
                        "key", "estrutura",
                        "title", "Estrutura",
                        "order", 1,
                        "items", List.of(
                                Map.of(
                                        "key", "quadro",
                                        "title", "Quadro em bom estado?",
                                        "description", "Verificar quadro",
                                        "required", true,
                                        "order", 1
                                ),
                                Map.of(
                                        "key", "iluminacao",
                                        "title", "Iluminacao adequada?",
                                        "description", "Verificar luzes",
                                        "required", true,
                                        "order", 2
                                )
                        )
                ))
        );
    }

    private UpdateChecklistAnswerCommand answer(
            String itemKey,
            ConformityAnswerValue value,
            String observation
    ) {
        return new UpdateChecklistAnswerCommand(itemKey, value, observation, Instant.now());
    }
}
