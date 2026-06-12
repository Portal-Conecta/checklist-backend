package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.shared.context.ContextClass;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.context.TypeUser;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FindChecklistExecutionByIdUseCaseTest {

    private final ChecklistExecutionRepository executionRepository = mock(ChecklistExecutionRepository.class);
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);
    private final FindChecklistExecutionByIdUseCase useCase = new FindChecklistExecutionByIdUseCase(
            executionRepository,
            contextProvider
    );

    @Test
    void shouldDenyAccessIfUserCannotAccessChecklistModule() {
        UUID executionId = UUID.randomUUID();
        RequestContext student = new RequestContext(UUID.randomUUID(), TypeUser.STUDENT);

        when(contextProvider.getRequestContext()).thenReturn(student);

        assertThrows(AccessDeniedException.class, () -> useCase.execute(executionId));
        verify(executionRepository, never()).findById(any());
    }

    @Test
    void shouldThrowExceptionIfExecutionNotFound() {
        UUID executionId = UUID.randomUUID();
        RequestContext manager = new RequestContext(UUID.randomUUID(), TypeUser.SENAI);

        when(contextProvider.getRequestContext()).thenReturn(manager);
        when(executionRepository.findById(executionId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(executionId));
    }

    @Test
    void shouldAllowManagerToFindAnyExecution() {
        UUID executionId = UUID.randomUUID();
        RequestContext manager = new RequestContext(UUID.randomUUID(), TypeUser.SENAI);
        ChecklistExecution execution = ChecklistExecution.builder()
                .id(executionId)
                .userId(UUID.randomUUID())
                .classId(UUID.randomUUID())
                .build();

        when(contextProvider.getRequestContext()).thenReturn(manager);
        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        ChecklistExecution result = useCase.execute(executionId);

        assertNotNull(result);
        assertEquals(execution, result);
    }

    @Test
    void shouldAllowOperationalUserToFindOwnExecutionForOperableClass() {
        UUID executionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        RequestContext teacher = new RequestContext(
                userId,
                TypeUser.TEACHER,
                List.of(new ContextClass(classId, "TEACHER"))
        );
        ChecklistExecution execution = ChecklistExecution.builder()
                .id(executionId)
                .userId(userId)
                .classId(classId)
                .build();

        when(contextProvider.getRequestContext()).thenReturn(teacher);
        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        ChecklistExecution result = useCase.execute(executionId);

        assertNotNull(result);
        assertEquals(execution, result);
    }

    @Test
    void shouldDenyAccessToOperationalUserIfExecutionIsOwnedBySomeoneElse() {
        UUID executionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        RequestContext teacher = new RequestContext(
                userId,
                TypeUser.TEACHER,
                List.of(new ContextClass(classId, "TEACHER"))
        );
        ChecklistExecution execution = ChecklistExecution.builder()
                .id(executionId)
                .userId(UUID.randomUUID()) // different user
                .classId(classId)
                .build();

        when(contextProvider.getRequestContext()).thenReturn(teacher);
        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        assertThrows(AccessDeniedException.class, () -> useCase.execute(executionId));
    }

    @Test
    void shouldDenyAccessToOperationalUserIfClassIsNotOperable() {
        UUID executionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        RequestContext teacher = new RequestContext(
                userId,
                TypeUser.TEACHER,
                List.of(new ContextClass(UUID.randomUUID(), "TEACHER")) // different class
        );
        ChecklistExecution execution = ChecklistExecution.builder()
                .id(executionId)
                .userId(userId)
                .classId(classId)
                .build();

        when(contextProvider.getRequestContext()).thenReturn(teacher);
        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        assertThrows(AccessDeniedException.class, () -> useCase.execute(executionId));
    }
}
