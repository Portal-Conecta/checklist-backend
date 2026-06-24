package com.portal.conecta.checklist.modules.checklist.application.usecase.template.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubRoomProvider;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistTemplateRepositoryPort;
import com.portal.conecta.checklist.modules.checklist.application.usecase.template.command.create.CreateChecklistTemplateCommand;
import com.portal.conecta.checklist.modules.checklist.application.usecase.template.command.create.CreateChecklistTemplateUseCase;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateChecklistTemplateUseCaseTest {

    private final ChecklistTemplateRepositoryPort templateRepository = mock(ChecklistTemplateRepositoryPort.class);
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
        CreateChecklistTemplateCommand command = command(roomId);

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        when(hubRoomProvider.existsById(roomId)).thenReturn(true);
        when(templateRepository.save(any(ChecklistTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChecklistTemplate result = useCase.execute(command);

        assertThat(result.getRoomId()).isEqualTo(roomId);
        assertThat(result.getTitle()).isEqualTo("Checklist padrao");
        verify(templateRepository).save(any(ChecklistTemplate.class));
    }

    @Test
    void shouldRejectWhenUserCannotManageTemplates() {
        CreateChecklistTemplateCommand command = command(UUID.randomUUID());

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.STUDENT));

        assertThrows(AccessDeniedException.class, () -> useCase.execute(command));

        verify(hubRoomProvider, never()).existsById(any());
        verify(templateRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenRoomDoesNotExistInHub() {
        UUID roomId = UUID.randomUUID();
        CreateChecklistTemplateCommand command = command(roomId);

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        when(hubRoomProvider.existsById(roomId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(command));

        verify(templateRepository, never()).save(any());
    }

    private CreateChecklistTemplateCommand command(UUID roomId) {
        return new CreateChecklistTemplateCommand(
                roomId,
                "Checklist padrao",
                "Descricao",
                schema("estrutura", "quadro")
        );
    }

    private ChecklistSchema schema(String sectionKey, String itemKey) {
        return new ChecklistSchema(List.of(new ChecklistSection(
                sectionKey,
                "Estrutura",
                1,
                List.of(new ChecklistItem(
                        itemKey,
                        "Quadro em bom estado?",
                        "Verificar quadro",
                        true,
                        1
                ))
        )));
    }

    private RequestContext user(TypeUser userType) {
        return new RequestContext(UUID.randomUUID(), userType);
    }
}
