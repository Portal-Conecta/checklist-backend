package com.portal.conecta.checklist.unit.issues.application.usecase.query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.access.AccessDeniedException;

import com.portal.conecta.checklist.module.issues.application.port.out.execution.ExecutionAccessPort;
import com.portal.conecta.checklist.module.issues.domain.model.ChecklistIssue;
import com.portal.conecta.checklist.module.issues.application.usecase.query.ListIssuesByExecutionUseCase;
import com.portal.conecta.checklist.module.issues.infrastructure.persistence.ChecklistIssueRepository;
import com.portal.conecta.checklist.shared.context.ClassRole;
import com.portal.conecta.checklist.shared.context.ContextClass;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.context.TypeUser;

import jakarta.persistence.EntityNotFoundException;

class ListIssuesByExecutionUseCaseTest {

    private final ChecklistIssueRepository issueRepository = mock(ChecklistIssueRepository.class);
    private final ExecutionAccessPort executionAccessPort = mock(ExecutionAccessPort.class);
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);
    private final ListIssuesByExecutionUseCase useCase = new ListIssuesByExecutionUseCase(
            issueRepository,
            executionAccessPort,
            contextProvider
    );

    @Test
    void shouldAllowManagementProfileToListIssuesFromAnyExecution() {
        UUID executionId = UUID.randomUUID();
        List<ChecklistIssue> issues = List.of(ChecklistIssue.builder().id(UUID.randomUUID()).build());

        when(executionAccessPort.findClassIdByExecutionId(executionId)).thenReturn(Optional.of(UUID.randomUUID()));
        when(contextProvider.getRequestContext()).thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.SENAI));
        when(issueRepository.findAllByExecutionId(executionId)).thenReturn(issues);

        List<ChecklistIssue> result = useCase.execute(executionId);

        assertEquals(issues, result);
        verify(issueRepository).findAllByExecutionId(executionId);
    }

    @Test
    void shouldAllowOperationalProfileToListIssuesFromLinkedClassExecution() {
        UUID executionId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        List<ChecklistIssue> issues = List.of(ChecklistIssue.builder().id(UUID.randomUUID()).build());

        when(executionAccessPort.findClassIdByExecutionId(executionId)).thenReturn(Optional.of(classId));
        when(contextProvider.getRequestContext()).thenReturn(representative(classId));
        when(issueRepository.findAllByExecutionId(executionId)).thenReturn(issues);

        List<ChecklistIssue> result = useCase.execute(executionId);

        assertEquals(issues, result);
        verify(issueRepository).findAllByExecutionId(executionId);
    }

    @Test
    void shouldRejectOperationalProfileFromAnotherClass() {
        UUID executionId = UUID.randomUUID();

        when(executionAccessPort.findClassIdByExecutionId(executionId)).thenReturn(Optional.of(UUID.randomUUID()));
        when(contextProvider.getRequestContext()).thenReturn(representative(UUID.randomUUID()));

        assertThrows(AccessDeniedException.class, () -> useCase.execute(executionId));

        verify(issueRepository, never()).findAllByExecutionId(executionId);
    }

    @Test
    void shouldRejectWhenExecutionDoesNotExist() {
        UUID executionId = UUID.randomUUID();

        when(executionAccessPort.findClassIdByExecutionId(executionId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(executionId));

        verify(contextProvider, never()).getRequestContext();
        verify(issueRepository, never()).findAllByExecutionId(executionId);
    }

    private RequestContext representative(UUID classId) {
        return new RequestContext(
                UUID.randomUUID(),
                TypeUser.REPRESENTATIVE,
                List.of(new ContextClass(classId, ClassRole.REPRESENTATIVE))
        );
    }
}
