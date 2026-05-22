package com.portal.conecta.checklist.module.checklist.presentation.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistTemplateCreateRequest;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistTemplateResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ChecklistTemplateMapper {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public ChecklistTemplateMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ChecklistTemplate toEntity(ChecklistTemplateCreateRequest request) {
        if (request == null) {
            return null;
        }

        return ChecklistTemplate.builder()
                .roomId(request.roomId())
                .title(request.title())
                .description(normalizeDescription(request.description()))
                .version(1)
                .status(ChecklistTemplateStatus.DRAFT)
                .active(false)
                .schemaJson(toSchemaJson(request.schemaJson()))
                .build();
    }

    public ChecklistTemplateResponseDTO toResponse(ChecklistTemplate template) {
        if (template == null) {
            return null;
        }

        return new ChecklistTemplateResponseDTO(
                template.getId(),
                template.getRoomId(),
                template.getTitle(),
                template.getDescription(),
                template.getVersion(),
                template.getStatus(),
                template.isActive(),
                toSchemaDTO(template.getSchemaJson()),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }

    public Map<String, Object> toSchemaJson(ChecklistSchemaDTO schemaJson) {
        if (schemaJson == null) {
            return Map.of();
        }

        return objectMapper.convertValue(schemaJson, MAP_TYPE);
    }

    public ChecklistSchemaDTO toSchemaDTO(Map<String, Object> schemaJson) {
        if (schemaJson == null || schemaJson.isEmpty()) {
            return new ChecklistSchemaDTO(List.of());
        }

        return objectMapper.convertValue(schemaJson, ChecklistSchemaDTO.class);
    }

    private String normalizeDescription(String description) {
        return description == null ? "" : description;
    }
}
