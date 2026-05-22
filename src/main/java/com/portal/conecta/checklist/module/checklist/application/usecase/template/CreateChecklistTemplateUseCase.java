package com.portal.conecta.checklist.module.checklist.application.usecase.template;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.infrastructure.client.hub.HubRoomClient;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistTemplateCreateRequest;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistItemDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistTemplateMapper;
import com.portal.conecta.checklist.shared.context.CurrentUserProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CreateChecklistTemplateUseCase {

    private final ChecklistTemplateRepository templateRepository;
    private final HubRoomClient hubRoomClient;
    private final CurrentUserProvider currentUserProvider;
    private final ObjectMapper objectMapper;
    private  final ChecklistTemplateMapper templateMapper;

    @Transactional
    public ChecklistTemplate execute(ChecklistTemplateCreateRequest request) {
        var currentUser = currentUserProvider.getCurrentUser();

        if (!currentUser.canManageChecklistTemplates()) {
            throw new IllegalArgumentException("Usuario nao tem permissao para criar templates de checklist.");
        }

        if (!hubRoomClient.existsById(request.roomId())) {
            throw new EntityNotFoundException("Sala nao encontrada no mock do Hub.");
        }

        validateStableKeys(request.schemaJson());

        var template =
                ChecklistTemplate.builder()
                .roomId(request.roomId())
                .title(request.title())
                .description(normalizeDescription(request.description()))
                .version(1)
                .status(ChecklistTemplateStatus.DRAFT)
                .active(false)
                .schemaJson(toJsonMap(request.schemaJson()))
                .build();

        return templateRepository.save(template);
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

    private String normalizeDescription(String description) {
        return description == null ? "" : description;
    }

    private Map<String, Object> toJsonMap(ChecklistSchemaDTO schema) {
        return objectMapper.convertValue(schema, new TypeReference<Map<String, Object>>() {});
    }
}
