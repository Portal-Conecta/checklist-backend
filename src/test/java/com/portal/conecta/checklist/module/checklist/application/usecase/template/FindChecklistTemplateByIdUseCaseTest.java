package com.portal.conecta.checklist.module.checklist.application.usecase.template;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import com.portal.conecta.checklist.shared.context.CurrentUserContext;
import com.portal.conecta.checklist.shared.context.CurrentUserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FindChecklistTemplateByIdUseCaseTest {

    private final ChecklistTemplateRepository templateRepository = mock(ChecklistTemplateRepository.class);
    private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
    private final FindChecklistTemplateByIdUseCase useCase = new FindChecklistTemplateByIdUseCase(
            templateRepository,
            currentUserProvider
    );

    @Test
    void shouldRejectApprenticeAccess() {
        UUID templateId = UUID.randomUUID();
        when(currentUserProvider.getCurrentUser()).thenReturn(apprentice());

        assertThrows(AccessDeniedException.class, () -> useCase.execute(templateId));

        verify(templateRepository, never()).findById(templateId);
    }

    @Test
    void shouldAllowNonApprenticeAccess() {
        UUID templateId = UUID.randomUUID();
        ChecklistTemplate template = ChecklistTemplate.builder().id(templateId).build();
        when(currentUserProvider.getCurrentUser()).thenReturn(senai());
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));

        ChecklistTemplate result = useCase.execute(templateId);

        assertSame(template, result);
        verify(templateRepository).findById(templateId);
    }

    private CurrentUserContext apprentice() {
        return new CurrentUserContext(UUID.randomUUID(), "Aprendiz", "aprendiz@exemplo.com", "APRENDIZ");
    }

    private CurrentUserContext senai() {
        return new CurrentUserContext(UUID.randomUUID(), "Senai", "senai@exemplo.com", "PERFIL_SENAI");
    }
}
