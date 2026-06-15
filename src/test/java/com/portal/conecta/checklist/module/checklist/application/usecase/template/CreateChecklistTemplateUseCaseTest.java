package com.portal.conecta.checklist.module.checklist.application.usecase.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistTemplateMapper;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.RoomReference;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistTemplateCreateRequest;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistItemDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSectionDTO;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateChecklistTemplateUseCaseTest {

    private final ChecklistTemplateRepository templateRepository = mock(ChecklistTemplateRepository.class);
    private final HubRoomProvider hubRoomProvider = mock(HubRoomProvider.class);
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);
    private final ChecklistTemplateMapper mapper = new ChecklistTemplateMapper(new ObjectMapper());
    private final CreateChecklistTemplateUseCase useCase = new CreateChecklistTemplateUseCase(
            templateRepository,
            hubRoomProvider,
            contextProvider,
            mapper
    );

    @Test
    void shouldCreateTemplateWhenManagerAndRoomExists() {
        UUID roomId = UUID.randomUUID();
        ChecklistTemplateCreateRequest request = request(roomId);

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        // Ajustado para o novo método findById
        when(hubRoomProvider.findById(roomId)).thenReturn(Optional.of(mock(RoomReference.class)));
        when(templateRepository.save(any(ChecklistTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChecklistTemplate result = useCase.execute(request);

        assertThat(result.getRoomId()).isEqualTo(roomId);
        assertThat(result.getTitle()).isEqualTo("Checklist padrao");
        verify(templateRepository).save(any(ChecklistTemplate.class));
    }

    @Test
    void shouldRejectWhenUserCannotManageTemplates() {
        ChecklistTemplateCreateRequest request = request(UUID.randomUUID());

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.STUDENT));

        assertThrows(AccessDeniedException.class, () -> useCase.execute(request));

        verify(hubRoomProvider, never()).findById(any());
        verify(templateRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenRoomDoesNotExistInHub() {
        UUID roomId = UUID.randomUUID();
        ChecklistTemplateCreateRequest request = request(roomId);

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        // Ajustado para simular que o Core não encontrou a sala
        when(hubRoomProvider.findById(roomId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(request));

        verify(templateRepository, never()).save(any());
    }

    private ChecklistTemplateCreateRequest request(UUID roomId) {
        return new ChecklistTemplateCreateRequest(
                roomId,
                "Checklist padrao",
                "Descricao",
                new ChecklistSchemaDTO(List.of(new ChecklistSectionDTO(
                        "estrutura",
                        "Estrutura",
                        1,
                        List.of(new ChecklistItemDTO(
                                "quadro",
                                "Quadro em bom estado?",
                                "Verificar quadro",
                                true,
                                1
                        ))
                )))
        );
    }

    private RequestContext user(TypeUser userType) {
        return new RequestContext(UUID.randomUUID(), userType);
    }
}