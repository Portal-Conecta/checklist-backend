package com.portal.conecta.checklist.modules.checklist.application.usecase.template.command;

import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubRoomProvider;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistTemplateRepositoryPort;
import com.portal.conecta.checklist.modules.checklist.application.usecase.template.command.activate.ActivateChecklistTemplateUseCase;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ActivateChecklistTemplateUseCaseTest {

    private final ChecklistTemplateRepositoryPort templateRepository = mock(ChecklistTemplateRepositoryPort.class);
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);
    private final HubRoomProvider hubRoomProvider = mock(HubRoomProvider.class);
    private final ActivateChecklistTemplateUseCase useCase = new ActivateChecklistTemplateUseCase(
            templateRepository, contextProvider, hubRoomProvider);

    @Test
    @DisplayName("deve ativar template DRAFT e inativar versao anterior do mesmo grupo")
    void deveAtivarTemplateDraftEInativarVersaoAnterior() {
        UUID groupId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();

        ChecklistTemplate draft = template(templateId, groupId, ChecklistTemplateStatus.DRAFT, false);
        ChecklistTemplate active = template(UUID.randomUUID(), groupId, ChecklistTemplateStatus.ACTIVE, true);

        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(draft));
        when(hubRoomProvider.findById(draft.getRoomId())).thenReturn(Optional.of(new RoomReference(draft.getRoomId())));
        when(templateRepository.findByTemplateGroupIdAndStatus(groupId, ChecklistTemplateStatus.ACTIVE))
                .thenReturn(List.of(active));
        when(templateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ChecklistTemplate result = useCase.execute(templateId);

        assertEquals(ChecklistTemplateStatus.ACTIVE, result.getStatus());
        assertTrue(result.isActive());
        assertEquals(ChecklistTemplateStatus.INACTIVE, active.getStatus());
        assertFalse(active.isActive());
        verify(templateRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("deve ativar template DRAFT sem versao anterior no grupo")
    void deveAtivarTemplateDraftSemVersaoAnterior() {
        UUID groupId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();

        ChecklistTemplate draft = template(templateId, groupId, ChecklistTemplateStatus.DRAFT, false);

        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(draft));
        when(hubRoomProvider.findById(draft.getRoomId())).thenReturn(Optional.of(new RoomReference(draft.getRoomId())));
        when(templateRepository.findByTemplateGroupIdAndStatus(groupId, ChecklistTemplateStatus.ACTIVE))
                .thenReturn(List.of());
        when(templateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ChecklistTemplate result = useCase.execute(templateId);

        assertEquals(ChecklistTemplateStatus.ACTIVE, result.getStatus());
        assertTrue(result.isActive());
        verify(templateRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("deve rejeitar quando template nao esta em DRAFT")
    void deveRejeitarQuandoTemplateNaoEstaDraft() {
        UUID groupId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();

        ChecklistTemplate active = template(templateId, groupId, ChecklistTemplateStatus.ACTIVE, true);

        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(active));

        assertThrows(IllegalStateException.class, () -> useCase.execute(templateId));

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
        UUID templateId = UUID.randomUUID();

        ChecklistTemplate draft = template(templateId, groupId, ChecklistTemplateStatus.DRAFT, false);

        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(draft));
        when(hubRoomProvider.findById(draft.getRoomId())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> useCase.execute(templateId));

        verify(templateRepository, never()).save(any());
    }

    private ChecklistTemplate template(UUID id, UUID groupId, ChecklistTemplateStatus status, boolean active) {
        return ChecklistTemplate.builder()
                .id(id)
                .roomId(UUID.randomUUID())
                .templateGroupId(groupId)
                .status(status)
                .active(active)
                .build();
    }

    private RequestContext senai() {
        return new RequestContext(UUID.randomUUID(), TypeUser.SENAI);
    }

    private RequestContext student() {
        return new RequestContext(UUID.randomUUID(), TypeUser.STUDENT);
    }
}
