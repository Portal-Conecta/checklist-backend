package com.portal.conecta.checklist.module.checklist.application.usecase.template;

import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.shared.context.ContextClass;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.context.TypeUser;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ListChecklistTemplatesUseCaseTest {

    private final ChecklistTemplateRepository templateRepository = mock(ChecklistTemplateRepository.class);
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);

    private final ListChecklistTemplatesUseCase useCase =
            new ListChecklistTemplatesUseCase(
                    templateRepository,
                    contextProvider
            );

    @Test
    void shouldRejectApprenticeAccess() {
        Pageable pageable = PageRequest.of(0, 10);

        when(contextProvider.getRequestContext())
                .thenReturn(apprentice());

        assertThrows(
                AccessDeniedException.class,
                () -> useCase.execute(pageable)
        );

        verify(templateRepository, never())
                .findAll(any(Pageable.class));
    }

    @Test
    void shouldAllowNonApprenticeAccess() {
        Pageable pageable = PageRequest.of(0, 10);

        when(contextProvider.getRequestContext())
                .thenReturn(senai());

        when(templateRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        useCase.execute(pageable);

        verify(templateRepository)
                .findAll(pageable);
    }

    @Test
    void shouldFilterActiveAndActiveStatusTemplatesForOperationalUser() {
        Pageable pageable = PageRequest.of(0, 10);
        UUID classId = UUID.randomUUID();

        when(contextProvider.getRequestContext())
                .thenReturn(teacher(classId));

        when(templateRepository.findByActiveTrueAndStatus(eq(ChecklistTemplateStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        useCase.execute(pageable);

        verify(templateRepository)
                .findByActiveTrueAndStatus(ChecklistTemplateStatus.ACTIVE, pageable);
        verify(templateRepository, never())
                .findAll(any(Pageable.class));
    }

    private RequestContext apprentice() {
        return new RequestContext(
                UUID.randomUUID(),
                TypeUser.STUDENT
        );
    }

    private RequestContext senai() {
        return new RequestContext(
                UUID.randomUUID(),
                TypeUser.SENAI
        );
    }

    private RequestContext teacher(UUID classId) {
        return new RequestContext(
                UUID.randomUUID(),
                TypeUser.TEACHER,
                List.of(new ContextClass(classId, "TEACHER"))
        );
    }
}