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
import java.util.UUID;

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
                .description(request.description() == null ? "" : request.description())
                .version(1)
                .status(ChecklistTemplateStatus.DRAFT)
                .active(false)
                .schemaJson(toSchemaJson(request.schemaJson()))
                .templateGroupId(UUID.randomUUID())
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

    public List<ChecklistTemplateResponseDTO> toResponseList(List<ChecklistTemplate> templates) {
        if (templates == null) {
            return List.of();
        }

        return templates.stream()
                .map(this::toResponse)
                .toList();
    }

    public ChecklistSchemaDTO toSchemaDTO(Map<String, Object> schemaJson) {
        if (schemaJson == null || schemaJson.isEmpty()) {
            return new ChecklistSchemaDTO(List.of());
        }

        return objectMapper.convertValue(schemaJson, ChecklistSchemaDTO.class);
    }

    private Map<String, Object> toSchemaJson(ChecklistSchemaDTO schemaJson) {
        if (schemaJson == null) {
            return Map.of();
        }

        return objectMapper.convertValue(schemaJson, MAP_TYPE);
    }
}
