package com.portal.conecta.checklist.modules.checklist.application.usecase.template.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistTemplateRepositoryPort;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSchema;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.template.response.ChecklistItemSearchResponseDTO;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import lombok.RequiredArgsConstructor;
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
public class SearchItemsByCategoryUseCase {

    private final ChecklistTemplateRepositoryPort templateRepository;
    private final RequestContextProvider contextProvider;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<ChecklistItemSearchResponseDTO> execute(String categoryName) {
        RequestContext currentUser = contextProvider.getRequestContext();

        if (!currentUser.canAccessChecklistModule()) {
            throw new AccessDeniedException("Usuario nao tem permissao para acessar o modulo Checklist.");
        }

        if (categoryName == null || categoryName.isBlank()) {
            return List.of();
        }

        String searchCategory = categoryName.trim();
        List<ChecklistTemplate> activeTemplates = templateRepository.findAllByActiveTrueAndStatus(ChecklistTemplateStatus.ACTIVE);
        List<ChecklistItemSearchResponseDTO> results = new ArrayList<>();

        for (ChecklistTemplate template : activeTemplates) {
            if (template.getSchemaJson() != null && !template.getSchemaJson().isEmpty()) {
                try {
                    ChecklistSchema schema = objectMapper.convertValue(template.getSchemaJson(), ChecklistSchema.class);
                    if (schema != null && schema.sections() != null) {
                        schema.sections().forEach(section -> {
                            if (section.items() != null) {
                                section.items().forEach(item -> {
                                    if (item.category() != null && item.category().trim().equalsIgnoreCase(searchCategory)) {
                                        results.add(new ChecklistItemSearchResponseDTO(
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
                    // Ignora erros de mapeamento caso algum template esteja com schema invalido
                }
            }
        }

        return results;
    }
}
