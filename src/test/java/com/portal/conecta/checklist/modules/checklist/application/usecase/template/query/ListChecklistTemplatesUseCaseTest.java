package com.portal.conecta.checklist.modules.checklist.application.usecase.template.query;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import com.portal.conecta.checklist.shared.context.ClassRole;
import com.portal.conecta.checklist.shared.context.ContextClass;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.context.TypeUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
            contextProvider);

    @Test
    void shouldRejectApprenticeAccess() {
        when(contextProvider.getRequestContext()).thenReturn(apprentice());

        assertThrows(AccessDeniedException.class, useCase::execute);

        verify(templateRepository, never()).findAll();
        verify(templateRepository, never()).findAllByActiveTrueAndStatus(ChecklistTemplateStatus.ACTIVE);
    }

    @Test
    void shouldAllowManagementAccessToAllTemplates() {
        List<ChecklistTemplate> templates = List.of(
                ChecklistTemplate.builder().id(UUID.randomUUID()).active(false).status(ChecklistTemplateStatus.DRAFT)
                        .build());

        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findAll()).thenReturn(templates);

        List<ChecklistTemplate> result = useCase.execute();

        assertEquals(templates, result);
        verify(templateRepository).findAll();
        verify(templateRepository, never()).findAllByActiveTrueAndStatus(ChecklistTemplateStatus.ACTIVE);
    }

    @Test
    void shouldAllowOperationalAccessToActiveTemplatesOnly() {
        List<ChecklistTemplate> templates = List.of(
                ChecklistTemplate.builder().id(UUID.randomUUID()).active(true).status(ChecklistTemplateStatus.ACTIVE)
                        .build());

        when(contextProvider.getRequestContext()).thenReturn(representative(UUID.randomUUID()));
        when(templateRepository.findAllByActiveTrueAndStatus(ChecklistTemplateStatus.ACTIVE)).thenReturn(templates);

        List<ChecklistTemplate> result = useCase.execute();

        assertEquals(templates, result);
        verify(templateRepository, never()).findAll();
        verify(templateRepository).findAllByActiveTrueAndStatus(ChecklistTemplateStatus.ACTIVE);
    }

    private RequestContext apprentice() {
        return new RequestContext(UUID.randomUUID(), TypeUser.STUDENT);
    }

    private RequestContext senai() {
        return new RequestContext(UUID.randomUUID(), TypeUser.SENAI);
    }

    private RequestContext representative(UUID classId) {
        return new RequestContext(
                UUID.randomUUID(),
                TypeUser.REPRESENTATIVE,
                List.of(new ContextClass(classId, ClassRole.REPRESENTATIVE)));
    }
}
