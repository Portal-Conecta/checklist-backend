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
import java.util.UUID;

/**
 * Caso de uso responsavel por listar templates de checklist.
 *
 * <p>A listagem respeita as regras de acesso do modulo e retorna os templates
 * existentes para usuarios autorizados. Opcionalmente, a listagem pode ser
 * filtrada por {@code roomId}, {@code status} e/ou {@link ChecklistCategory}
 * (grupo de itens da sala), aplicados sobre o conjunto de templates ja
 * restrito pelas regras de acesso do usuario.</p>
 */
@Service
@RequiredArgsConstructor
public class ListChecklistTemplatesUseCase {

    private final ChecklistTemplateRepositoryPort templateRepository;
    private final RequestContextProvider contextProvider;

    @Transactional(readOnly = true)
    public List<ChecklistTemplate> execute() {
        return execute(null, null, null);
    }

    @Transactional(readOnly = true)
    public List<ChecklistTemplate> execute(UUID roomId, ChecklistTemplateStatus status, ChecklistCategory category) {
        RequestContext currentUser = contextProvider.getRequestContext();

        if (!currentUser.canAccessChecklistModule()) {
            throw new AccessDeniedException("Usuario nao tem permissao para acessar o modulo Checklist.");
        }

        List<ChecklistTemplate> templates;
        if (currentUser.canManageChecklistTemplates()) {
            templates = category == null
                    ? templateRepository.findAll()
                    : templateRepository.findAllByCategory(category);
        } else {
            templates = category == null
                    ? templateRepository.findAllByActiveTrueAndStatus(ChecklistTemplateStatus.ACTIVE)
                    : templateRepository.findAllByActiveTrueAndStatusAndCategory(ChecklistTemplateStatus.ACTIVE, category);
        }

        return templates.stream()
                .filter(template -> roomId == null || roomId.equals(template.getRoomId()))
                .filter(template -> status == null || status.equals(template.getStatus()))
                .toList();
    }
}
