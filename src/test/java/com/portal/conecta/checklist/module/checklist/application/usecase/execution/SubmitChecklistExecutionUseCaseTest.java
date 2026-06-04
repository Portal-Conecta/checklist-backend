package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.submit.SubmitChecklistExecutionUseCase;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.enums.ConformityAnswerValue;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistAnswerRequestDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import com.portal.conecta.checklist.module.issues.domain.enums.IssuePriority;
import com.portal.conecta.checklist.module.issues.domain.enums.IssueStatus;
import com.portal.conecta.checklist.module.issues.presentation.mapper.ChecklistIssueMapper;
import com.portal.conecta.checklist.shared.context.ContextClass;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.context.TypeUser;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SubmitChecklistExecutionUseCaseTest {

    private final ChecklistExecutionRepository executionRepository = mock(ChecklistExecutionRepository.class);
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final ChecklistExecutionMapper executionMapper = new ChecklistExecutionMapper(
            objectMapper,
            new ChecklistIssueMapper()
    );
    private final SubmitChecklistExecutionUseCase useCase = new SubmitChecklistExecutionUseCase(
            executionRepository,
            executionMapper,
            objectMapper,
            contextProvider
    );

    @Test
    void shouldSubmitChecklistAndCreateIssueForNonCompliantAnswer() {
        UUID executionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistExecution execution = draftExecution(executionId, userId, classId);
        ChecklistExecutionSubmitDTO request = new ChecklistExecutionSubmitDTO(List.of(
                answer("quadro", ConformityAnswerValue.COMPLIANT, null),
                answer("iluminacao", ConformityAnswerValue.NON_COMPLIANT, "Lampada queimada")
        ));

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(currentUser(userId, classId));
        when(executionRepository.save(execution)).thenReturn(execution);

        ChecklistExecution result = useCase.execute(executionId, request);

        assertThat(result.getStatus()).isEqualTo(ChecklistExecutionStatus.SUBMITTED);
        assertThat(result.getSubmittedAt()).isNotNull();
        assertThat(result.getComplianceScore()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.getIssues()).hasSize(1);
        assertThat(result.getIssues().getFirst().getItemKey()).isEqualTo("iluminacao");
        assertThat(result.getIssues().getFirst().getItemTitleSnapshot()).isEqualTo("Iluminacao adequada?");
        assertThat(result.getIssues().getFirst().getDescription()).isEqualTo("Lampada queimada");
        assertThat(result.getIssues().getFirst().getStatus()).isEqualTo(IssueStatus.OPEN);
        assertThat(result.getIssues().getFirst().getPriority()).isEqualTo(IssuePriority.MEDIUM);
        assertThat(result.getIssues().getFirst().getAssignedUserReference().getUserId()).isEqualTo(userId);
        verify(executionRepository).save(execution);
    }

    @Test
    void shouldRejectNonCompliantAnswerWithoutObservation() {
        UUID executionId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistExecution execution = draftExecution(executionId, UUID.randomUUID(), classId);
        UUID userId = execution.getUserId();
        ChecklistExecutionSubmitDTO request = new ChecklistExecutionSubmitDTO(List.of(
                answer("quadro", ConformityAnswerValue.COMPLIANT, null),
                answer("iluminacao", ConformityAnswerValue.NON_COMPLIANT, " ")
        ));

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(currentUser(userId, classId));

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(executionId, request));

        verify(executionRepository, never()).save(execution);
    }

    @Test
    void shouldRejectMissingRequiredAnswer() {
        UUID executionId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistExecution execution = draftExecution(executionId, UUID.randomUUID(), classId);
        UUID userId = execution.getUserId();
        ChecklistExecutionSubmitDTO request = new ChecklistExecutionSubmitDTO(List.of(
                answer("quadro", ConformityAnswerValue.COMPLIANT, null)
        ));

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(currentUser(userId, classId));

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(executionId, request));

        verify(executionRepository, never()).save(execution);
    }

    @Test
    void shouldRejectSubmittingChecklistThatIsNotDraft() {
        UUID executionId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistExecution execution = draftExecution(executionId, UUID.randomUUID(), classId);
        execution.setStatus(ChecklistExecutionStatus.SUBMITTED);
        UUID userId = execution.getUserId();
        ChecklistExecutionSubmitDTO request = new ChecklistExecutionSubmitDTO(List.of(
                answer("quadro", ConformityAnswerValue.COMPLIANT, null),
                answer("iluminacao", ConformityAnswerValue.COMPLIANT, null)
        ));

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(currentUser(userId, classId));

        assertThrows(IllegalStateException.class, () -> useCase.execute(executionId, request));

        verify(executionRepository, never()).save(execution);
    }

    @Test
    void shouldRejectSubmittingExecutionFromAnotherUser() {
        UUID executionId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistExecution execution = draftExecution(executionId, UUID.randomUUID(), classId);
        ChecklistExecutionSubmitDTO request = new ChecklistExecutionSubmitDTO(List.of(
                answer("quadro", ConformityAnswerValue.COMPLIANT, null),
                answer("iluminacao", ConformityAnswerValue.COMPLIANT, null)
        ));

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(currentUser(UUID.randomUUID(), classId));

        assertThrows(AccessDeniedException.class, () -> useCase.execute(executionId, request));

        verify(executionRepository, never()).save(execution);
    }

    @Test
    void shouldRejectSubmittingExecutionFromManagementProfileEvenWhenOwner() {
        UUID executionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        ChecklistExecution execution = draftExecution(executionId, userId, classId);
        ChecklistExecutionSubmitDTO request = new ChecklistExecutionSubmitDTO(List.of(
                answer("quadro", ConformityAnswerValue.COMPLIANT, null),
                answer("iluminacao", ConformityAnswerValue.COMPLIANT, null)
        ));

        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(contextProvider.getRequestContext()).thenReturn(new RequestContext(
                userId,
                TypeUser.SENAI,
                List.of(new ContextClass(classId, "TEACHER"))
        ));

        assertThrows(AccessDeniedException.class, () -> useCase.execute(executionId, request));

        verify(executionRepository, never()).save(execution);
    }

    private ChecklistExecution draftExecution(UUID executionId, UUID userId, UUID classId) {
        return ChecklistExecution.builder()
                .id(executionId)
                .userId(userId)
                .classId(classId)
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
                List.of(new ContextClass(classId, "REPRESENTATIVE"))
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

    private ChecklistAnswerRequestDTO answer(
            String itemKey,
            ConformityAnswerValue value,
            String observation
    ) {
        return new ChecklistAnswerRequestDTO(itemKey, value, observation, Instant.now());
    }
}
