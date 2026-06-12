package com.portal.conecta.checklist.modules.checklist.application.usecase.template.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.modules.checklist.application.usecase.template.command.CreateChecklistTemplateUseCase;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import com.portal.conecta.checklist.modules.checklist.application.usecase.template.command.CreateChecklistTemplateCommand;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistItem;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSchema;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSection;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.context.TypeUser;
import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubRoomProvider;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
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
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CreateChecklistTemplateUseCase useCase = new CreateChecklistTemplateUseCase(
            templateRepository,
            hubRoomProvider,
            contextProvider,
            objectMapper
    );

    @Test
    void shouldCreateTemplateWhenManagerAndRoomExists() {
        UUID roomId = UUID.randomUUID();
        CreateChecklistTemplateCommand request = request(roomId);

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        when(hubRoomProvider.existsById(roomId)).thenReturn(true);
        when(templateRepository.save(any(ChecklistTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChecklistTemplate result = useCase.execute(request);

        assertThat(result.getRoomId()).isEqualTo(roomId);
        assertThat(result.getTitle()).isEqualTo("Checklist padrao");
        verify(templateRepository).save(any(ChecklistTemplate.class));
    }

    @Test
    void shouldRejectWhenUserCannotManageTemplates() {
        CreateChecklistTemplateCommand request = request(UUID.randomUUID());

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.STUDENT));

        assertThrows(AccessDeniedException.class, () -> useCase.execute(request));

        verify(hubRoomProvider, never()).existsById(any());
        verify(templateRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenRoomDoesNotExistInHub() {
        UUID roomId = UUID.randomUUID();
        CreateChecklistTemplateCommand request = request(roomId);

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        when(hubRoomProvider.existsById(roomId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(request));

        verify(templateRepository, never()).save(any());
    }

    private CreateChecklistTemplateCommand request(UUID roomId) {
        return new CreateChecklistTemplateCommand(
                roomId,
                "Checklist padrao",
                "Descricao",
                new ChecklistSchema(List.of(new ChecklistSection(
                        "estrutura",
                        "Estrutura",
                        1,
                        List.of(new ChecklistItem(
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
