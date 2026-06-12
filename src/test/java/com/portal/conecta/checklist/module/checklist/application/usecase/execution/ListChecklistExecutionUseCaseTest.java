package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.shared.context.ContextClass;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.context.TypeUser;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListChecklistExecutionUseCaseTest {

    private final ChecklistExecutionRepository executionRepository = mock(ChecklistExecutionRepository.class);
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);
    private final ListChecklistExecutionUseCase useCase = new ListChecklistExecutionUseCase(
            executionRepository,
            contextProvider
    );

    @Test
    void shouldDenyAccessIfUserCannotAccessChecklistModule() {
        Pageable pageable = PageRequest.of(0, 10);
        RequestContext student = new RequestContext(UUID.randomUUID(), TypeUser.STUDENT);

        when(contextProvider.getRequestContext()).thenReturn(student);

        assertThrows(AccessDeniedException.class, () -> useCase.execute(pageable));
        verify(executionRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void shouldAllowManagerToSeeAllExecutions() {
        Pageable pageable = PageRequest.of(0, 10);
        RequestContext manager = new RequestContext(UUID.randomUUID(), TypeUser.SENAI);
        Page<ChecklistExecution> page = new PageImpl<>(List.of());

        when(contextProvider.getRequestContext()).thenReturn(manager);
        when(executionRepository.findAll(pageable)).thenReturn(page);

        Page<ChecklistExecution> result = useCase.execute(pageable);

        assertNotNull(result);
        verify(executionRepository).findAll(pageable);
        verify(executionRepository, never()).findAllAllowedForOperational(any(), any());
    }

    @Test
    void shouldDenyAccessIfOperationalUserHasNoClasses() {
        Pageable pageable = PageRequest.of(0, 10);
        RequestContext teacher = new RequestContext(
                UUID.randomUUID(),
                TypeUser.TEACHER,
                List.of()
        );

        when(contextProvider.getRequestContext()).thenReturn(teacher);

        assertThrows(AccessDeniedException.class, () -> useCase.execute(pageable));
        verify(executionRepository, never()).findAll(any(Pageable.class));
        verify(executionRepository, never()).findAllAllowedForOperational(any(), any());
    }

    @Test
    void shouldFilterExecutionsForOperationalUserWithClasses() {
        Pageable pageable = PageRequest.of(0, 10);
        UUID classId = UUID.randomUUID();
        RequestContext teacher = new RequestContext(
                UUID.randomUUID(),
                TypeUser.TEACHER,
                List.of(new ContextClass(classId, "TEACHER"))
        );
        Page<ChecklistExecution> page = new PageImpl<>(List.of());

        when(contextProvider.getRequestContext()).thenReturn(teacher);
        when(executionRepository.findAllAllowedForOperational(List.of(classId), pageable)).thenReturn(page);

        Page<ChecklistExecution> result = useCase.execute(pageable);

        assertNotNull(result);
        verify(executionRepository).findAllAllowedForOperational(List.of(classId), pageable);
        verify(executionRepository, never()).findAll(any(Pageable.class));
    }
}
