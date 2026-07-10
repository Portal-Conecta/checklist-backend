package com.portal.conecta.checklist.modules.checklist.application.usecase.template.query.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistTemplateRepositoryPort;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSchema;
<<<<<<< HEAD
import com.portal.conecta.checklist.modules.checklist.presentation.dto.template.response.ChecklistItemByCategorySearchResponseDTO;
=======
import com.portal.conecta.checklist.modules.checklist.presentation.dto.template.response.ChecklistItemSearchResponseDTO;
>>>>>>> 02e7cb3b7e26872d52e06b8aae70a43777a962eb
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
<<<<<<< HEAD
    public List<ChecklistItemByCategorySearchResponseDTO> execute(String categoryName) {
=======
    public List<ChecklistItemSearchResponseDTO> execute(String categoryName) {
>>>>>>> 02e7cb3b7e26872d52e06b8aae70a43777a962eb
        RequestContext currentUser = contextProvider.getRequestContext();

        if (!currentUser.canAccessChecklistModule()) {
            throw new AccessDeniedException("Usuario nao tem permissao para acessar o modulo Checklist.");
        }

        if (categoryName == null || categoryName.isBlank()) {
            return List.of();
        }

        String searchCategory = categoryName.trim();
        List<ChecklistTemplate> activeTemplates = templateRepository.findAllByActiveTrueAndStatus(ChecklistTemplateStatus.ACTIVE);
<<<<<<< HEAD
        List<ChecklistItemByCategorySearchResponseDTO> results = new ArrayList<>();
=======
        List<ChecklistItemSearchResponseDTO> results = new ArrayList<>();
>>>>>>> 02e7cb3b7e26872d52e06b8aae70a43777a962eb

        for (ChecklistTemplate template : activeTemplates) {
            if (template.getSchemaJson() != null && !template.getSchemaJson().isEmpty()) {
                try {
                    ChecklistSchema schema = objectMapper.convertValue(template.getSchemaJson(), ChecklistSchema.class);
                    if (schema != null && schema.sections() != null) {
                        schema.sections().forEach(section -> {
                            if (section.items() != null) {
                                section.items().forEach(item -> {
                                    if (item.category() != null && item.category().trim().equalsIgnoreCase(searchCategory)) {
<<<<<<< HEAD
                                        results.add(new ChecklistItemByCategorySearchResponseDTO(
=======
                                        results.add(new ChecklistItemSearchResponseDTO(
>>>>>>> 02e7cb3b7e26872d52e06b8aae70a43777a962eb
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
