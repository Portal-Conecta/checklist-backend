package com.portal.conecta.checklist.unit.checklist.application.usecase.template.query;

import com.portal.conecta.checklist.modules.checklist.application.usecase.template.query.list.ListChecklistTemplatesUseCase;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistCategory;
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
        contextProvider
    );

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
            ChecklistTemplate.builder().id(UUID.randomUUID()).active(false).status(ChecklistTemplateStatus.DRAFT).build()
        );

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
            ChecklistTemplate.builder().id(UUID.randomUUID()).active(true).status(ChecklistTemplateStatus.ACTIVE).build()
        );

        when(contextProvider.getRequestContext()).thenReturn(representative(UUID.randomUUID()));
        when(templateRepository.findAllByActiveTrueAndStatus(ChecklistTemplateStatus.ACTIVE)).thenReturn(templates);

        List<ChecklistTemplate> result = useCase.execute();

        assertEquals(templates, result);
        verify(templateRepository, never()).findAll();
        verify(templateRepository).findAllByActiveTrueAndStatus(ChecklistTemplateStatus.ACTIVE);
    }

    @Test
    void shouldFilterTemplatesByCategoryForManagement() {
        List<ChecklistTemplate> templates = List.of(
                ChecklistTemplate.builder()
                        .id(UUID.randomUUID())
                        .category(ChecklistCategory.MOVEIS)
                        .status(ChecklistTemplateStatus.ACTIVE)
                        .build()
        );

        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findAllByCategory(ChecklistCategory.MOVEIS)).thenReturn(templates);

        List<ChecklistTemplate> result = useCase.execute(null, null, ChecklistCategory.MOVEIS);

        assertEquals(templates, result);
        verify(templateRepository).findAllByCategory(ChecklistCategory.MOVEIS);
        verify(templateRepository, never()).findAll();
    }

    @Test
    void shouldReturnAllTemplatesWhenNoFilterIsProvided() {
        List<ChecklistTemplate> templates = List.of(
            ChecklistTemplate.builder().id(UUID.randomUUID()).roomId(UUID.randomUUID()).status(ChecklistTemplateStatus.DRAFT).build(),
            ChecklistTemplate.builder().id(UUID.randomUUID()).roomId(UUID.randomUUID()).status(ChecklistTemplateStatus.ACTIVE).build()
        );

        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findAll()).thenReturn(templates);

        List<ChecklistTemplate> result = useCase.execute(null, null, null);

        assertEquals(templates, result);
        verify(templateRepository).findAll();
    }

    @Test
    void shouldFilterOnlyByRoomIdWhenStatusIsAbsent() {
        UUID targetRoom = UUID.randomUUID();
        ChecklistTemplate matching = ChecklistTemplate.builder().id(UUID.randomUUID()).roomId(targetRoom).status(ChecklistTemplateStatus.DRAFT).build();
        ChecklistTemplate other = ChecklistTemplate.builder().id(UUID.randomUUID()).roomId(UUID.randomUUID()).status(ChecklistTemplateStatus.ACTIVE).build();

        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findAll()).thenReturn(List.of(matching, other));

        List<ChecklistTemplate> result = useCase.execute(targetRoom, null, null);

        assertEquals(List.of(matching), result);
    }

    @Test
    void shouldFilterOnlyByStatusWhenRoomIdIsAbsent() {
        ChecklistTemplate matching = ChecklistTemplate.builder().id(UUID.randomUUID()).roomId(UUID.randomUUID()).status(ChecklistTemplateStatus.ACTIVE).build();
        ChecklistTemplate other = ChecklistTemplate.builder().id(UUID.randomUUID()).roomId(UUID.randomUUID()).status(ChecklistTemplateStatus.DRAFT).build();

        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findAll()).thenReturn(List.of(matching, other));

        List<ChecklistTemplate> result = useCase.execute(null, ChecklistTemplateStatus.ACTIVE, null);

        assertEquals(List.of(matching), result);
    }

    @Test
    void shouldFilterByRoomIdAndStatusTogether() {
        UUID targetRoom = UUID.randomUUID();
        ChecklistTemplate matching = ChecklistTemplate.builder().id(UUID.randomUUID()).roomId(targetRoom).status(ChecklistTemplateStatus.ACTIVE).build();
        ChecklistTemplate sameRoomWrongStatus = ChecklistTemplate.builder().id(UUID.randomUUID()).roomId(targetRoom).status(ChecklistTemplateStatus.DRAFT).build();
        ChecklistTemplate sameStatusWrongRoom = ChecklistTemplate.builder().id(UUID.randomUUID()).roomId(UUID.randomUUID()).status(ChecklistTemplateStatus.ACTIVE).build();

        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findAll()).thenReturn(List.of(matching, sameRoomWrongStatus, sameStatusWrongRoom));

        List<ChecklistTemplate> result = useCase.execute(targetRoom, ChecklistTemplateStatus.ACTIVE, null);

        assertEquals(List.of(matching), result);
    }

    @Test
    void shouldApplyFiltersOnTopOfOperationalAccessRestriction() {
        UUID targetRoom = UUID.randomUUID();
        ChecklistTemplate matching = ChecklistTemplate.builder().id(UUID.randomUUID()).roomId(targetRoom).active(true).status(ChecklistTemplateStatus.ACTIVE).build();
        ChecklistTemplate otherRoom = ChecklistTemplate.builder().id(UUID.randomUUID()).roomId(UUID.randomUUID()).active(true).status(ChecklistTemplateStatus.ACTIVE).build();

        when(contextProvider.getRequestContext()).thenReturn(representative(UUID.randomUUID()));
        when(templateRepository.findAllByActiveTrueAndStatus(ChecklistTemplateStatus.ACTIVE)).thenReturn(List.of(matching, otherRoom));

        List<ChecklistTemplate> result = useCase.execute(targetRoom, ChecklistTemplateStatus.ACTIVE, null);

        assertEquals(List.of(matching), result);
        verify(templateRepository, never()).findAll();
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
            List.of(new ContextClass(classId, ClassRole.REPRESENTATIVE))
        );
    }
}
