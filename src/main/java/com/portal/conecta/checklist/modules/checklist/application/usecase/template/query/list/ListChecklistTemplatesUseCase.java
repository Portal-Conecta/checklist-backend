package com.portal.conecta.checklist.modules.checklist.application.usecase.template.query.list;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistCategory;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistTemplateRepositoryPort;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Caso de uso responsavel por listar templates de checklist.
 *
 * <p>A listagem respeita as regras de acesso do modulo e retorna os templates
 * existentes para usuarios autorizados. Aceita filtro opcional por
 * {@link ChecklistCategory} (grupo de itens da sala).</p>
 */
@Service
@RequiredArgsConstructor
public class ListChecklistTemplatesUseCase {

    private final ChecklistTemplateRepositoryPort templateRepository;
    private final RequestContextProvider contextProvider;

    @Transactional(readOnly = true)
    public List<ChecklistTemplate> execute() {
        return execute(null);
    }

    @Transactional(readOnly = true)
    public List<ChecklistTemplate> execute(ChecklistCategory category) {
        RequestContext currentUser = contextProvider.getRequestContext();

        if (!currentUser.canAccessChecklistModule()) {
            throw new AccessDeniedException("Usuario nao tem permissao para acessar o modulo Checklist.");
        }

        if (currentUser.canManageChecklistTemplates()) {
            if (category == null) {
                return templateRepository.findAll();
            }
            return templateRepository.findAllByCategory(category);
        }

        if (category == null) {
            return templateRepository.findAllByActiveTrueAndStatus(ChecklistTemplateStatus.ACTIVE);
        }
        return templateRepository.findAllByActiveTrueAndStatusAndCategory(
                ChecklistTemplateStatus.ACTIVE,
                category
        );
    }
}
