package com.portal.conecta.checklist.module.checklist.application.usecase.template.query.find;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.application.port.out.persistence.ChecklistTemplateRepositoryPort;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso responsavel por buscar um template de checklist por identificador.
 *
 * <p>Antes de retornar o template, valida se o usuario autenticado possui
 * permissao para acessar o modulo de checklist.</p>
 */
@Service
@RequiredArgsConstructor
public class FindChecklistTemplateByIdUseCase {

    private final ChecklistTemplateRepositoryPort templateRepository;
    private final RequestContextProvider contextProvider;

    @Transactional(readOnly = true)
    public ChecklistTemplate execute(UUID templateId) {
        RequestContext currentUser = contextProvider.getRequestContext();

        if (!currentUser.canAccessChecklistModule()) {
            throw new AccessDeniedException("Usuario nao tem permissao para acessar o modulo Checklist.");
        }

        ChecklistTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Template de checklist nao encontrado."));

        if (!currentUser.canManageChecklistTemplates() && !isActiveTemplate(template)) {
            throw new AccessDeniedException("Usuario nao tem permissao para acessar este template de checklist.");
        }

        return template;
    }

    private boolean isActiveTemplate(ChecklistTemplate template) {
        return template.isActive() && template.getStatus() == ChecklistTemplateStatus.ACTIVE;
    }
}
