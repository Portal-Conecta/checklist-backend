package com.portal.conecta.checklist.module.checklist.application.usecase.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.RoomReference;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistItemDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSectionDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.update.ChecklistTemplateEditRequest;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.context.TypeUser;
import com.portal.conecta.checklist.shared.hub.provider.room.HubRoomProvider;
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
        when(hubRoomProvider.findById(template.getRoomId())).thenReturn(Optional.of(mock(RoomReference.class)));
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
        when(hubRoomProvider.findById(existing.getRoomId())).thenReturn(Optional.of(mock(RoomReference.class)));
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
        verify(hubRoomProvider, never()).findById(any());
        verify(templateRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenTemplateNotFound() {
        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        when(templateRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> useCase.execute(UUID.randomUUID(), requestFull()));

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
                () -> useCase.execute(templateId, requestFull()));

        verify(hubRoomProvider, never()).findById(any());
        verify(templateRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenRoomIsDeletedInHub() {
        UUID templateId = UUID.randomUUID();
        ChecklistTemplate draft = draftTemplate();

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(draft));
        // Simula o Hub retornando que a sala não existe (foi removida)
        when(hubRoomProvider.findById(draft.getRoomId())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> useCase.execute(templateId, requestFull()));

        verify(templateRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenSchemaHasDuplicateKeys() {
        UUID templateId = UUID.randomUUID();
        ChecklistTemplate template = draftTemplate();

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(hubRoomProvider.findById(template.getRoomId())).thenReturn(Optional.of(mock(RoomReference.class)));

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(templateId, requestDuplicateKeys()));

        verify(templateRepository, never()).save(any());
    }

    private ChecklistTemplateEditRequest requestFull() {
        return new ChecklistTemplateEditRequest(
                "Novo título",
                "Nova descrição",
                schema("secao-1", "item-1")
        );
    }

    private ChecklistTemplateEditRequest requestOnlySchema() {
        return new ChecklistTemplateEditRequest(null, null, schema("secao-1", "item-1"));
    }

    private ChecklistTemplateEditRequest requestDuplicateKeys() {
        return new ChecklistTemplateEditRequest(
                null, null,
                new ChecklistSchemaDTO(List.of(
                        new ChecklistSectionDTO("secao-1", "Seção 1", 1, List.of(
                                new ChecklistItemDTO("item-1", "Item 1", "", true, 1),
                                new ChecklistItemDTO("item-1", "Item duplicado", "", true, 2)
                        ))
                ))
        );
    }

    private ChecklistSchemaDTO schema(String sectionKey, String itemKey) {
        return new ChecklistSchemaDTO(List.of(
                new ChecklistSectionDTO(sectionKey, "Seção", 1, List.of(
                        new ChecklistItemDTO(itemKey, "Item", "Observação", true, 1)
                ))
        ));
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