package com.portal.conecta.checklist.modules.checklist.application.usecase.template.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistItem;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSchema;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSection;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.context.TypeUser;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EditChecklistTemplateUseCaseTest {

    private final ChecklistTemplateRepository templateRepository = mock(ChecklistTemplateRepository.class);
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EditChecklistTemplateUseCase useCase = new EditChecklistTemplateUseCase(
            templateRepository,
            contextProvider,
            objectMapper);

    @Test
    void shouldEditTemplateWhenDraftAndManager() {
        UUID templateId = UUID.randomUUID();

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(draftTemplate()));
        when(templateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ChecklistTemplate result = useCase.execute(templateId, requestFull());

        assertThat(result.getTitle()).isEqualTo("Novo título");
        verify(templateRepository).save(any());
    }

    @Test
    void shouldKeepCurrentValuesWhenFieldsAreNull() {
        UUID templateId = UUID.randomUUID();
        ChecklistTemplate existing = draftTemplate();
        String originalTitle = existing.getTitle();

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(existing));
        when(templateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ChecklistTemplate result = useCase.execute(templateId, requestOnlySchema());

        assertThat(result.getTitle()).isEqualTo(originalTitle);
    }

    @Test
    void shouldRejectWhenUserCannotManageTemplates() {
        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.STUDENT));

        assertThrows(AccessDeniedException.class,
                () -> useCase.execute(UUID.randomUUID(), requestFull()));

        verify(templateRepository, never()).findById(any());
        verify(templateRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenTemplateNotFound() {
        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        when(templateRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> useCase.execute(UUID.randomUUID(), requestFull()));

        verify(templateRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenTemplateIsNotDraft() {
        UUID templateId = UUID.randomUUID();
        ChecklistTemplate active = draftTemplate();
        active.setStatus(ChecklistTemplateStatus.ACTIVE);

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(active));

        assertThrows(IllegalStateException.class,
                () -> useCase.execute(templateId, requestFull()));

        verify(templateRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenSchemaHasDuplicateKeys() {
        UUID templateId = UUID.randomUUID();

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(draftTemplate()));

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(templateId, requestDuplicateKeys()));

        verify(templateRepository, never()).save(any());
    }

    private EditChecklistTemplateCommand requestFull() {
        return new EditChecklistTemplateCommand(
                "Novo título",
                "Nova descrição",
                schema("secao-1", "item-1"));
    }

    private EditChecklistTemplateCommand requestOnlySchema() {
        return new EditChecklistTemplateCommand(null, null, schema("secao-1", "item-1"));
    }

    private EditChecklistTemplateCommand requestDuplicateKeys() {
        return new EditChecklistTemplateCommand(
                null, null,
                new ChecklistSchema(List.of(
                        new ChecklistSection("secao-1", "Seção 1", 1, List.of(
                                new ChecklistItem("item-1", "Item 1", "", true, 1),
                                new ChecklistItem("item-1", "Item duplicado", "", true, 2))))));
    }

    private ChecklistSchema schema(String sectionKey, String itemKey) {
        return new ChecklistSchema(List.of(
                new ChecklistSection(sectionKey, "Seção", 1, List.of(
                        new ChecklistItem(itemKey, "Item", "Observação", true, 1)))));
    }

    private ChecklistTemplate draftTemplate() {
        ChecklistTemplate t = new ChecklistTemplate();
        t.setTitle("Título original");
        t.setDescription("Descrição original");
        t.setRoomId(UUID.randomUUID());
        t.setStatus(ChecklistTemplateStatus.DRAFT);
        return t;
    }

    private RequestContext user(TypeUser userType) {
        return new RequestContext(UUID.randomUUID(), userType);
    }
}
