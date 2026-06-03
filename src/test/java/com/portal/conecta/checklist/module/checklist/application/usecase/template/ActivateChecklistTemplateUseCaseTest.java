package com.portal.conecta.checklist.module.checklist.application.usecase.template;

import com.portal.conecta.checklist.module.checklist.application.usecase.template.command.ActivateChecklistTemplateUseCase;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistTemplateRepository;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ActivateChecklistTemplateUseCaseTest {

    private final ChecklistTemplateRepository templateRepository = mock(ChecklistTemplateRepository.class);
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);
    private final ActivateChecklistTemplateUseCase useCase = new ActivateChecklistTemplateUseCase(templateRepository, contextProvider);

    @Test
    @DisplayName("deve ativar template e desativar outros templates ativos da mesma sala")
    void deveAtivarTemplateEDesativarOutrosTemplatesAtivosDaMesmaSala() {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        ChecklistTemplate template = template(templateId, roomId, ChecklistTemplateStatus.DRAFT, false);
        ChecklistTemplate activeTemplate = template(UUID.randomUUID(), roomId, ChecklistTemplateStatus.ACTIVE, true);

        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(templateRepository.findByRoomIdAndActiveTrueAndStatus(roomId, ChecklistTemplateStatus.ACTIVE))
                .thenReturn(List.of(activeTemplate));
        when(templateRepository.save(template)).thenReturn(template);

        ChecklistTemplate result = useCase.execute(templateId);

        assertSame(template, result);
        assertEquals(ChecklistTemplateStatus.ACTIVE, template.getStatus());
        assertTrue(template.isActive());
        assertEquals(ChecklistTemplateStatus.INACTIVE, activeTemplate.getStatus());
        assertFalse(activeTemplate.isActive());
        verify(templateRepository).saveAll(List.of(activeTemplate));
        verify(templateRepository).save(template);
    }

    @Test
    @DisplayName("deve manter idempotente quando template ja esta ativo")
    void deveManterIdempotenteQuandoTemplateJaEstaAtivo() {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        ChecklistTemplate template = template(templateId, roomId, ChecklistTemplateStatus.ACTIVE, true);

        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(templateRepository.findByRoomIdAndActiveTrueAndStatus(roomId, ChecklistTemplateStatus.ACTIVE))
                .thenReturn(List.of(template));
        when(templateRepository.save(template)).thenReturn(template);

        ChecklistTemplate result = useCase.execute(templateId);

        assertSame(template, result);
        assertEquals(ChecklistTemplateStatus.ACTIVE, template.getStatus());
        assertTrue(template.isActive());
        verify(templateRepository, never()).saveAll(any());
        verify(templateRepository).save(template);
    }

    @Test
    @DisplayName("deve rejeitar quando template nao existe")
    void deveRejeitarQuandoTemplateNaoExiste() {
        UUID templateId = UUID.randomUUID();

        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findById(templateId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(templateId));

        verify(templateRepository, never()).findByRoomIdAndActiveTrueAndStatus(any(UUID.class), eq(ChecklistTemplateStatus.ACTIVE));
    }

    @Test
    @DisplayName("deve rejeitar quando usuario nao gerencia templates")
    void deveRejeitarQuandoUsuarioNaoGerenciaTemplates() {
        UUID templateId = UUID.randomUUID();

        when(contextProvider.getRequestContext()).thenReturn(student());

        assertThrows(AccessDeniedException.class, () -> useCase.execute(templateId));

        verify(templateRepository, never()).findById(templateId);
    }

    private ChecklistTemplate template(
            UUID templateId,
            UUID roomId,
            ChecklistTemplateStatus status,
            boolean active
    ) {
        return ChecklistTemplate.builder()
                .id(templateId)
                .roomId(roomId)
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
