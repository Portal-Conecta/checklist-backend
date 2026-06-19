package com.portal.conecta.checklist.modules.checklist.presentation.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistItem;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSchema;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.template.request.ChecklistTemplateCreateRequest;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.template.response.ChecklistItemSearchResponseDTO;
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

    public ChecklistTemplate toEntity(ChecklistTemplateCreateRequest request) {
        if (request == null) {
            return null;
        }

        ChecklistTemplate template = new ChecklistTemplate();
        template.setRoomId(request.roomId());
        template.setTitle(request.title());
        template.setDescription(request.description());

        if (request.schemaJson() != null) {
            Map<String, Object> mappedSchema = objectMapper.convertValue(
                    request.schemaJson(),
                    new TypeReference<Map<String, Object>>() {}
            );
            template.setSchemaJson(mappedSchema);
        }

        template.setActive(true);

        return template;
    }

    public ChecklistItemSearchResponseDTO toItemSearchResponseDTO(ChecklistItem item){
        if (item == null) return null;

        return new ChecklistItemSearchResponseDTO(
                item.key(),
                item.title(),
                item.description(),
                item.required(),
                item.order()
        );
    }

}
