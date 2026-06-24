package com.portal.conecta.checklist.modules.checklist.application.usecase.template.command;

import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubRoomProvider;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistTemplateRepositoryPort;
import com.portal.conecta.checklist.modules.checklist.application.usecase.template.command.create.CreateChecklistTemplateVersionUseCase;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.domain.valueobject.RoomReference;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.context.TypeUser;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateChecklistTemplateVersionUseCaseTest {

    private final ChecklistTemplateRepositoryPort templateRepository = mock(ChecklistTemplateRepositoryPort.class);
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);
    private final HubRoomProvider hubRoomProvider = mock(HubRoomProvider.class);
    private final CreateChecklistTemplateVersionUseCase useCase = new CreateChecklistTemplateVersionUseCase(
            templateRepository, contextProvider, hubRoomProvider);

    @Test
    @DisplayName("deve criar nova versao DRAFT copiando template ACTIVE")
    void deveCriarNovaVersaoDraftCopiandoTemplateActive() {
        UUID groupId = UUID.randomUUID();
        ChecklistTemplate origin = template(UUID.randomUUID(), groupId, ChecklistTemplateStatus.ACTIVE, 1);

        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findById(origin.getId())).thenReturn(Optional.of(origin));
        when(hubRoomProvider.findById(origin.getRoomId())).thenReturn(Optional.of(new RoomReference(origin.getRoomId())));
        when(templateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ChecklistTemplate result = useCase.execute(origin.getId());

        assertEquals(ChecklistTemplateStatus.DRAFT, result.getStatus());
        assertFalse(result.isActive());
        assertEquals(groupId, result.getTemplateGroupId());
        assertEquals(2, result.getVersion());
        assertNull(result.getId());
        verify(templateRepository).save(any());
    }

    @Test
    @DisplayName("deve rejeitar quando template nao esta ACTIVE")
    void deveRejeitarQuandoTemplateNaoEstaActive() {
        UUID groupId = UUID.randomUUID();
        ChecklistTemplate draft = template(UUID.randomUUID(), groupId, ChecklistTemplateStatus.DRAFT, 1);

        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findById(draft.getId())).thenReturn(Optional.of(draft));

        assertThrows(IllegalStateException.class, () -> useCase.execute(draft.getId()));

        verify(hubRoomProvider, never()).findById(any());
        verify(templateRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve rejeitar quando template nao existe")
    void deveRejeitarQuandoTemplateNaoExiste() {
        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(UUID.randomUUID()));

        verify(hubRoomProvider, never()).findById(any());
        verify(templateRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve rejeitar quando usuario nao gerencia templates")
    void deveRejeitarQuandoUsuarioNaoGerenciaTemplates() {
        when(contextProvider.getRequestContext()).thenReturn(student());

        assertThrows(AccessDeniedException.class, () -> useCase.execute(UUID.randomUUID()));

        verify(templateRepository, never()).findById(any());
        verify(hubRoomProvider, never()).findById(any());
    }

    @Test
    @DisplayName("deve rejeitar quando a sala do template nao existe mais no Hub")
    void deveRejeitarQuandoSalaRemovidaNoHub() {
        UUID groupId = UUID.randomUUID();
        ChecklistTemplate origin = template(UUID.randomUUID(), groupId, ChecklistTemplateStatus.ACTIVE, 1);

        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findById(origin.getId())).thenReturn(Optional.of(origin));
        when(hubRoomProvider.findById(origin.getRoomId())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> useCase.execute(origin.getId()));

        verify(templateRepository, never()).save(any());
    }

    private ChecklistTemplate template(UUID id, UUID groupId, ChecklistTemplateStatus status, int version) {
        return ChecklistTemplate.builder()
                .id(id)
                .templateGroupId(groupId)
                .roomId(UUID.randomUUID())
                .title("Template teste")
                .description("Descricao teste")
                .version(version)
                .status(status)
                .active(status == ChecklistTemplateStatus.ACTIVE)
                .build();
    }

    private RequestContext senai() {
        return new RequestContext(UUID.randomUUID(), TypeUser.SENAI);
    }

    private RequestContext student() {
        return new RequestContext(UUID.randomUUID(), TypeUser.STUDENT);
    }
}
