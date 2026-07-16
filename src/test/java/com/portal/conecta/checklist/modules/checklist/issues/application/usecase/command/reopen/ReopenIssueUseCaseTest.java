package com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command.reopen;

import com.portal.conecta.checklist.modules.checklist.issues.application.port.out.persistence.ChecklistIssueRepositoryPort;
import com.portal.conecta.checklist.modules.checklist.issues.domain.enums.IssueStatus;
import com.portal.conecta.checklist.modules.checklist.issues.domain.exception.InvalidIssueTransitionException;
import com.portal.conecta.checklist.modules.checklist.issues.domain.model.ChecklistIssue;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.context.TypeUser;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReopenIssueUseCaseTest {

    private final ChecklistIssueRepositoryPort repository = mock(ChecklistIssueRepositoryPort.class);
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);
    private final ReopenIssueUseCase useCase = new ReopenIssueUseCase(repository, contextProvider);

    @Test
    void shouldAllowSenaiToReopenIssue() {
        UUID issueId = UUID.randomUUID();
        ChecklistIssue issue = ChecklistIssue.builder()
                .id(issueId)
                .status(IssueStatus.RESOLVED)
                .build();

        when(contextProvider.getRequestContext()).thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.SENAI));
        when(repository.findById(issueId)).thenReturn(Optional.of(issue));
        when(repository.save(any(ChecklistIssue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChecklistIssue result = useCase.execute(issueId);

        assertEquals(IssueStatus.REOPENED, result.getStatus());
        verify(repository).save(issue);
    }

    @Test
    void shouldAllowWegToReopenIssue() {
        UUID issueId = UUID.randomUUID();
        ChecklistIssue issue = ChecklistIssue.builder()
                .id(issueId)
                .status(IssueStatus.RESOLVED)
                .build();

        when(contextProvider.getRequestContext()).thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.WEG));
        when(repository.findById(issueId)).thenReturn(Optional.of(issue));
        when(repository.save(any(ChecklistIssue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChecklistIssue result = useCase.execute(issueId);

        assertEquals(IssueStatus.REOPENED, result.getStatus());
        verify(repository).save(issue);
    }

    @Test
    void shouldRejectWhenUserIsStudent() {
        UUID issueId = UUID.randomUUID();

        when(contextProvider.getRequestContext()).thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.STUDENT));

        assertThrows(AccessDeniedException.class, () -> useCase.execute(issueId));
        verify(repository, never()).findById(any());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectWhenIssueDoesNotExist() {
        UUID issueId = UUID.randomUUID();

        when(contextProvider.getRequestContext()).thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.SENAI));
        when(repository.findById(issueId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(issueId));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectInvalidTransition() {
        UUID issueId = UUID.randomUUID();
        ChecklistIssue issue = ChecklistIssue.builder()
                .id(issueId)
                .status(IssueStatus.OPEN)
                .build();

        when(contextProvider.getRequestContext()).thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.SENAI));
        when(repository.findById(issueId)).thenReturn(Optional.of(issue));

        assertThrows(InvalidIssueTransitionException.class, () -> useCase.execute(issueId));
        verify(repository, never()).save(any());
    }
}
