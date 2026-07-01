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
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);
    private final FindChecklistTemplateByIdUseCase useCase = new FindChecklistTemplateByIdUseCase(
            templateRepository,
            contextProvider);

    @Test
    void shouldRejectApprenticeAccess() {
        UUID templateId = UUID.randomUUID();
        when(contextProvider.getRequestContext()).thenReturn(apprentice());

        assertThrows(AccessDeniedException.class, () -> useCase.execute(templateId));

        verify(templateRepository, never()).findById(templateId);
    }

    @Test
    void shouldAllowManagementAccessToAnyTemplateStatus() {
        UUID templateId = UUID.randomUUID();
        ChecklistTemplate template = ChecklistTemplate.builder()
                .id(templateId)
                .active(false)
                .status(ChecklistTemplateStatus.DRAFT)
                .build();
        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));

        ChecklistTemplate result = useCase.execute(templateId);

        assertSame(template, result);
        verify(templateRepository).findById(templateId);
    }

    @Test
    void shouldAllowOperationalAccessToActiveTemplate() {
        UUID templateId = UUID.randomUUID();
        ChecklistTemplate template = ChecklistTemplate.builder()
                .id(templateId)
                .active(true)
                .status(ChecklistTemplateStatus.ACTIVE)
                .build();
        when(contextProvider.getRequestContext()).thenReturn(representative(UUID.randomUUID()));
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));

        ChecklistTemplate result = useCase.execute(templateId);

        assertSame(template, result);
        verify(templateRepository).findById(templateId);
    }

    @Test
    void shouldRejectOperationalAccessToInactiveTemplate() {
        UUID templateId = UUID.randomUUID();
        ChecklistTemplate template = ChecklistTemplate.builder()
                .id(templateId)
                .active(false)
                .status(ChecklistTemplateStatus.INACTIVE)
                .build();
        when(contextProvider.getRequestContext()).thenReturn(representative(UUID.randomUUID()));
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));

        assertThrows(AccessDeniedException.class, () -> useCase.execute(templateId));

        verify(templateRepository).findById(templateId);
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
