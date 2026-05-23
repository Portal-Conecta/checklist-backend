package com.portal.conecta.checklist.module.checklist.application.usecase.template;

import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import com.portal.conecta.checklist.shared.context.CurrentUserContext;
import com.portal.conecta.checklist.shared.context.CurrentUserProvider;
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
    private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
    private final ListChecklistTemplatesUseCase useCase = new ListChecklistTemplatesUseCase(
            templateRepository,
            currentUserProvider
    );

    @Test
    void shouldRejectApprenticeAccess() {
        when(currentUserProvider.getCurrentUser()).thenReturn(apprentice());

        assertThrows(AccessDeniedException.class, useCase::execute);

        verify(templateRepository, never()).findAll();
    }

    @Test
    void shouldAllowNonApprenticeAccess() {
        when(currentUserProvider.getCurrentUser()).thenReturn(senai());
        when(templateRepository.findAll()).thenReturn(List.of());

        useCase.execute();

        verify(templateRepository).findAll();
    }

    private CurrentUserContext apprentice() {
        return new CurrentUserContext(UUID.randomUUID(), "Aprendiz", "aprendiz@exemplo.com", "APRENDIZ");
    }

    private CurrentUserContext senai() {
        return new CurrentUserContext(UUID.randomUUID(), "Senai", "senai@exemplo.com", "PERFIL_SENAI");
    }
}
