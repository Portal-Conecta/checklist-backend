package com.portal.conecta.checklist.module.checklist.presentation.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistTemplateResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ChecklistTemplateMapper {

    private final ObjectMapper objectMapper;

    public ChecklistTemplateMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
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

    public Page<ChecklistTemplateResponseDTO> toResponsePage(Page<ChecklistTemplate> templates) {
        return templates.map(this::toResponse);
    }

    public ChecklistSchemaDTO toSchemaDTO(Map<String, Object> schemaJson) {
        if (schemaJson == null || schemaJson.isEmpty()) {
            return new ChecklistSchemaDTO(List.of());
        }

        return objectMapper.convertValue(schemaJson, ChecklistSchemaDTO.class);
    }
}
