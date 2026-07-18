package com.portal.conecta.checklist.module.checklist.application.usecase.template.query.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.application.port.out.persistence.ChecklistTemplateRepositoryPort;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.domain.schema.ChecklistSchema;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Caso de uso responsavel por buscar itens por categoria nos templates ativos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchItemsByCategoryUseCase {

    private final ChecklistTemplateRepositoryPort templateRepository;
    private final RequestContextProvider contextProvider;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<ChecklistItemByCategoryResult> execute(String categoryName) {
        RequestContext currentUser = contextProvider.getRequestContext();

        if (!currentUser.canAccessChecklistModule()) {
            throw new AccessDeniedException("Usuario nao tem permissao para acessar o modulo Checklist.");
        }

        if (categoryName == null || categoryName.isBlank()) {
            return List.of();
        }

        String searchCategory = categoryName.trim();
        List<ChecklistTemplate> activeTemplates = templateRepository.findAllByActiveTrueAndStatus(ChecklistTemplateStatus.ACTIVE);
        List<ChecklistItemByCategoryResult> results = new ArrayList<>();

        for (ChecklistTemplate template : activeTemplates) {
            if (template.getSchemaJson() != null && !template.getSchemaJson().isEmpty()) {
                try {
                    ChecklistSchema schema = objectMapper.convertValue(template.getSchemaJson(), ChecklistSchema.class);
                    if (schema != null && schema.sections() != null) {
                        schema.sections().forEach(section -> {
                            if (section.items() != null) {
                                section.items().forEach(item -> {
                                    if (item.category() != null && item.category().trim().equalsIgnoreCase(searchCategory)) {
                                        results.add(new ChecklistItemByCategoryResult(
                                                template.getId(),
                                                template.getTitle(),
                                                section.key(),
                                                section.title(),
                                                item.key(),
                                                item.title(),
                                                item.description(),
                                                item.required(),
                                                item.order(),
                                                item.category()
                                        ));
                                    }
                                });
                            }
                        });
                    }
                } catch (Exception e) {
                    log.warn("Erro ao desserializar schema do template {}: {}", template.getId(), e.getMessage());
                }
            }
        }

        return results;
    }
}
