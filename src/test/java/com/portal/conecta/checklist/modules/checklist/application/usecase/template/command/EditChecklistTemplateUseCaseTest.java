package com.portal.conecta.checklist.modules.checklist.application.usecase.template.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubRoomProvider;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistTemplateRepositoryPort;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistItem;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSchema;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSection;
import com.portal.conecta.checklist.modules.checklist.domain.valueobject.RoomReference;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EditChecklistTemplateUseCaseTest {

    private final ChecklistTemplateRepositoryPort templateRepository = mock(ChecklistTemplateRepositoryPort.class);
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HubRoomProvider hubRoomProvider = mock(HubRoomProvider.class);
    private final EditChecklistTemplateUseCase useCase = new EditChecklistTemplateUseCase(
            templateRepository,
            contextProvider,
            objectMapper,
            hubRoomProvider
    );

    @Test
    void shouldEditTemplateWhenDraftAndManager() {
        UUID templateId = UUID.randomUUID();
        ChecklistTemplate template = draftTemplate();

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(hubRoomProvider.findById(template.getRoomId())).thenReturn(Optional.of(new RoomReference(template.getRoomId())));
        when(templateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ChecklistTemplate result = useCase.execute(templateId, commandFull());

        assertThat(result.getTitle()).isEqualTo("Novo titulo");
        verify(templateRepository).save(any());
    }

    @Test
    void shouldKeepCurrentValuesWhenFieldsAreNull() {
        UUID templateId = UUID.randomUUID();
        ChecklistTemplate existing = draftTemplate();
        String originalTitle = existing.getTitle();

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(existing));
        when(hubRoomProvider.findById(existing.getRoomId())).thenReturn(Optional.of(new RoomReference(existing.getRoomId())));
        when(templateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ChecklistTemplate result = useCase.execute(templateId, commandOnlySchema());

        assertThat(result.getTitle()).isEqualTo(originalTitle);
    }

    @Test
    void shouldRejectWhenUserCannotManageTemplates() {
        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.STUDENT));

        assertThrows(AccessDeniedException.class,
                () -> useCase.execute(UUID.randomUUID(), commandFull()));

        verify(templateRepository, never()).findById(any());
        verify(hubRoomProvider, never()).findById(any());
        verify(templateRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenTemplateNotFound() {
        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        when(templateRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> useCase.execute(UUID.randomUUID(), commandFull()));

        verify(hubRoomProvider, never()).findById(any());
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
                () -> useCase.execute(templateId, commandFull()));

        verify(hubRoomProvider, never()).findById(any());
        verify(templateRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenRoomIsDeletedInHub() {
        UUID templateId = UUID.randomUUID();
        ChecklistTemplate draft = draftTemplate();

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(draft));
        when(hubRoomProvider.findById(draft.getRoomId())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> useCase.execute(templateId, commandFull()));

        verify(templateRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenSchemaHasDuplicateKeys() {
        UUID templateId = UUID.randomUUID();
        ChecklistTemplate template = draftTemplate();

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(hubRoomProvider.findById(template.getRoomId())).thenReturn(Optional.of(new RoomReference(template.getRoomId())));

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(templateId, commandDuplicateKeys()));

        verify(templateRepository, never()).save(any());
    }

    private EditChecklistTemplateCommand commandFull() {
        return new EditChecklistTemplateCommand(
                "Novo titulo",
                "Nova descricao",
                schema("secao-1", "item-1")
        );
    }

    private EditChecklistTemplateCommand commandOnlySchema() {
        return new EditChecklistTemplateCommand(null, null, schema("secao-1", "item-1"));
    }

    private EditChecklistTemplateCommand commandDuplicateKeys() {
        return new EditChecklistTemplateCommand(
                null,
                null,
                new ChecklistSchema(List.of(
                        new ChecklistSection("secao-1", "Secao 1", 1, List.of(
                                new ChecklistItem("item-1", "Item 1", "", true, 1),
                                new ChecklistItem("item-1", "Item duplicado", "", true, 2)
                        ))
                ))
        );
    }

    private ChecklistSchema schema(String sectionKey, String itemKey) {
        return new ChecklistSchema(List.of(
                new ChecklistSection(sectionKey, "Secao", 1, List.of(
                        new ChecklistItem(itemKey, "Item", "Observacao", true, 1)
                ))
        ));
    }

    private ChecklistTemplate draftTemplate() {
        ChecklistTemplate template = new ChecklistTemplate();
        template.setTitle("Titulo original");
        template.setDescription("Descricao original");
        template.setRoomId(UUID.randomUUID());
        template.setStatus(ChecklistTemplateStatus.DRAFT);
        return template;
    }

    private RequestContext user(TypeUser userType) {
        return new RequestContext(UUID.randomUUID(), userType);
    }
}
