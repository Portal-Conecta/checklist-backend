package com.portal.conecta.checklist.module.checklist.application.usecase.template;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivateChecklistTemplateUseCase {

    private final ChecklistTemplateRepository templateRepository;
    private final RequestContextProvider contextProvider;

    @Transactional
    public ChecklistTemplate execute(UUID templateId) {
        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canManageChecklistTemplates()) {
            throw new AccessDeniedException("Usuario nao tem permissao para ativar templates de checklist.");
        }

        ChecklistTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Template de checklist nao encontrado."));

        deactivateOtherActiveTemplates(template);

        template.setStatus(ChecklistTemplateStatus.ACTIVE);
        template.setActive(true);

        return templateRepository.save(template);
    }

    private void deactivateOtherActiveTemplates(ChecklistTemplate template) {
        var templatesToDeactivate = templateRepository
                .findByRoomIdAndActiveTrueAndStatus(template.getRoomId(), ChecklistTemplateStatus.ACTIVE)
                .stream()
                .filter(activeTemplate -> !Objects.equals(activeTemplate.getId(), template.getId()))
                .peek(activeTemplate -> {
                    activeTemplate.setStatus(ChecklistTemplateStatus.INACTIVE);
                    activeTemplate.setActive(false);
                })
                .toList();

        if (!templatesToDeactivate.isEmpty()) {
            templateRepository.saveAll(templatesToDeactivate);
        }
    }
}
