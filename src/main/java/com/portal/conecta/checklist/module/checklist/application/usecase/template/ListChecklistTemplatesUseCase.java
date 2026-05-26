package com.portal.conecta.checklist.module.checklist.application.usecase.template;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListChecklistTemplatesUseCase {

    private final ChecklistTemplateRepository templateRepository;
    private final RequestContextProvider contextProvider;

    @Transactional(readOnly = true)
    public List<ChecklistTemplate> execute() {
        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canAccessChecklistModule()) {
            throw new AccessDeniedException("Usuario nao tem permissao para acessar o modulo Checklist.");
        }

        return templateRepository.findAll();
    }
}
