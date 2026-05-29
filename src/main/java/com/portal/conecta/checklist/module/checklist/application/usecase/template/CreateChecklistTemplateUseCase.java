package com.portal.conecta.checklist.module.checklist.application.usecase.template;

import com.portal.conecta.checklist.module.checklist.application.mapper.ChecklistTemplateCommandMapper;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.domain.validation.ChecklistTemplateLimits;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistTemplateCreateRequest;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistItemDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.hub.provider.room.HubRoomProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CreateChecklistTemplateUseCase {

    private final ChecklistTemplateRepository templateRepository;
    private final HubRoomProvider hubRoomProvider;
    private final RequestContextProvider contextProvider;
    private final ChecklistTemplateCommandMapper templateMapper;

    @Transactional
    public ChecklistTemplate execute(ChecklistTemplateCreateRequest request) {
        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canManageChecklistTemplates()) {
            throw new AccessDeniedException("Usuario nao tem permissao para criar templates de checklist.");
        }

        validateTemplateSize(request.schemaJson());

        if (!hubRoomProvider.existsById(request.roomId())) {
            throw new EntityNotFoundException("Sala nao encontrada no Hub.");
        }

        validateStableKeys(request.schemaJson());

        var template = templateMapper.toEntity(request);
        return templateRepository.save(template);
    }

    private void validateTemplateSize(ChecklistSchemaDTO schema) {
        int sectionCount = schema.sections().size();

        if (sectionCount > ChecklistTemplateLimits.MAX_SECTIONS) {
            throw new IllegalArgumentException(
                    "schemaJson.sections excede o limite de " + ChecklistTemplateLimits.MAX_SECTIONS + " secoes."
            );
        }

        int totalItems = 0;
        for (var section : schema.sections()) {
            int sectionItems = section.items().size();

            if (sectionItems > ChecklistTemplateLimits.MAX_ITEMS_PER_SECTION) {
                throw new IllegalArgumentException(
                        "section.items excede o limite de "
                                + ChecklistTemplateLimits.MAX_ITEMS_PER_SECTION
                                + " itens por secao: "
                                + section.key()
                );
            }

            totalItems += sectionItems;
            if (totalItems > ChecklistTemplateLimits.MAX_TOTAL_ITEMS) {
                throw new IllegalArgumentException(
                        "schemaJson excede o limite total de "
                                + ChecklistTemplateLimits.MAX_TOTAL_ITEMS
                                + " itens."
                );
            }
        }
    }

    private void validateStableKeys(ChecklistSchemaDTO schema) {
        Set<String> sectionKeys = new HashSet<>();
        Set<String> itemKeys = new HashSet<>();

        schema.sections().forEach(section -> {
            if (!sectionKeys.add(section.key())) {
                throw new IllegalArgumentException("section.key duplicado: " + section.key());
            }

            section.items().stream()
                    .map(ChecklistItemDTO::key)
                    .forEach(itemKey -> {
                        if (!itemKeys.add(itemKey)) {
                            throw new IllegalArgumentException("item.key duplicado: " + itemKey);
                        }
                    });
        });
    }
}
