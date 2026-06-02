package com.portal.conecta.checklist.module.checklist.application.usecase.template;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistTemplateRepository;
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

    private final ChecklistTemplateRepository templateRepository;
    private final RequestContextProvider contextProvider;

    @Transactional(readOnly = true)
    public ChecklistTemplate execute(UUID templateId) {
        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canAccessChecklistModule()) {
            throw new AccessDeniedException("Usuario nao tem permissao para acessar o modulo Checklist.");
        }

        return templateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Template de checklist nao encontrado."));
    }
}
