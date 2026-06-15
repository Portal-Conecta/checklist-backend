package com.portal.conecta.checklist.modules.checklist.presentation.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSchema;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.template.response.ChecklistTemplateResponseDTO;
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
                toSchema(template.getSchemaJson()),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }

    public List<ChecklistTemplateResponseDTO> toResponseList(List<ChecklistTemplate> templates) {
        return templates == null
                ? List.of()
                : templates.stream().map(this::toResponse).toList();
    }

    public ChecklistSchema toSchema(Map<String, Object> schemaJson) {
        return schemaJson == null || schemaJson.isEmpty()
                ? new ChecklistSchema(List.of())
                : objectMapper.convertValue(schemaJson, ChecklistSchema.class);
    }
}
