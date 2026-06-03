package com.portal.conecta.checklist.module.checklist.application.usecase.template;

import com.portal.conecta.checklist.module.checklist.application.usecase.template.query.ListChecklistTemplatesUseCase;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.context.TypeUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ListChecklistTemplatesUseCaseTest {

    private final ChecklistTemplateRepository templateRepository = mock(ChecklistTemplateRepository.class);
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);
    private final ListChecklistTemplatesUseCase useCase = new ListChecklistTemplatesUseCase(
            templateRepository,
            contextProvider
    );

    @Test
    void shouldRejectApprenticeAccess() {
        when(contextProvider.getRequestContext()).thenReturn(apprentice());

        assertThrows(AccessDeniedException.class, useCase::execute);

        verify(templateRepository, never()).findAll();
    }

    @Test
    void shouldAllowNonApprenticeAccess() {
        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findAll()).thenReturn(List.of());

        useCase.execute();

        verify(templateRepository).findAll();
    }

    private RequestContext apprentice() {
        return new RequestContext(UUID.randomUUID(), TypeUser.STUDENT);
    }

    private RequestContext senai() {
        return new RequestContext(UUID.randomUUID(), TypeUser.SENAI);
    }
}
