package com.portal.conecta.checklist.modules.checklist.application.usecase.template.command;

import com.portal.conecta.checklist.modules.checklist.application.usecase.template.command.ActivateChecklistTemplateUseCase;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.infrastructure.persistence.ChecklistTemplateRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ActivateChecklistTemplateUseCaseTest {

    private final ChecklistTemplateRepository templateRepository = mock(ChecklistTemplateRepository.class);
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);
    private final ActivateChecklistTemplateUseCase useCase = new ActivateChecklistTemplateUseCase(
            templateRepository, contextProvider);

    @Test
    @DisplayName("deve ativar template DRAFT e inativar versão anterior do mesmo grupo")
    void deveAtivarTemplateDraftEInativarVersaoAnterior() {
        UUID groupId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();

        ChecklistTemplate draft = template(templateId, groupId, ChecklistTemplateStatus.DRAFT, false);
        ChecklistTemplate active = template(UUID.randomUUID(), groupId, ChecklistTemplateStatus.ACTIVE, true);

        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(draft));
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
    @DisplayName("deve ativar template DRAFT sem versão anterior no grupo")
    void deveAtivarTemplateDraftSemVersaoAnterior() {
        UUID groupId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();

        ChecklistTemplate draft = template(templateId, groupId, ChecklistTemplateStatus.DRAFT, false);

        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(draft));
        when(templateRepository.findByTemplateGroupIdAndStatus(groupId, ChecklistTemplateStatus.ACTIVE))
                .thenReturn(List.of());
        when(templateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ChecklistTemplate result = useCase.execute(templateId);

        assertEquals(ChecklistTemplateStatus.ACTIVE, result.getStatus());
        assertTrue(result.isActive());
        verify(templateRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("deve rejeitar quando template não está em DRAFT")
    void deveRejeitarQuandoTemplateNaoEstaDraft() {
        UUID groupId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();

        ChecklistTemplate active = template(templateId, groupId, ChecklistTemplateStatus.ACTIVE, true);

        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(active));

        assertThrows(IllegalStateException.class, () -> useCase.execute(templateId));

        verify(templateRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve rejeitar quando template não existe")
    void deveRejeitarQuandoTemplateNaoExiste() {
        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(UUID.randomUUID()));

        verify(templateRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve rejeitar quando usuario não gerencia templates")
    void deveRejeitarQuandoUsuarioNaoGerenciaTemplates() {
        when(contextProvider.getRequestContext()).thenReturn(student());

        assertThrows(AccessDeniedException.class, () -> useCase.execute(UUID.randomUUID()));

        verify(templateRepository, never()).findById(any());
    }

    private ChecklistTemplate template(UUID id, UUID groupId, ChecklistTemplateStatus status, boolean active) {
        return ChecklistTemplate.builder()
                .id(id)
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
