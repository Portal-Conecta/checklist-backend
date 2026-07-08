package com.portal.conecta.checklist.modules.checklist.issues.application.usecase.command;

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

class RestartProgressIssueUseCaseTest {

    private final ChecklistIssueRepositoryPort repository = mock(ChecklistIssueRepositoryPort.class);
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);
    private final RestartProgressIssueUseCase useCase = new RestartProgressIssueUseCase(repository, contextProvider);

    @Test
    void shouldAllowWegOrSenaiToRestartProgress() {
        UUID issueId = UUID.randomUUID();
        ChecklistIssue issue = ChecklistIssue.builder()
                .id(issueId)
                .status(IssueStatus.REOPENED)
                .build();

        when(contextProvider.getRequestContext()).thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.WEG));
        when(repository.findById(issueId)).thenReturn(Optional.of(issue));
        when(repository.save(any(ChecklistIssue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChecklistIssue result = useCase.execute(issueId);

        assertEquals(IssueStatus.IN_PROGRESS, result.getStatus());
        verify(repository).save(issue);
    }

    @Test
    void shouldRejectWhenUserIsNotWegOrSenai() {
        UUID issueId = UUID.randomUUID();

        when(contextProvider.getRequestContext()).thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.TEACHER));

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
