package com.portal.conecta.checklist.module.checklist.presentation.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistTemplateResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChecklistTemplateMapper {

    private final ObjectMapper objectMapper;

    public ChecklistTemplateResponseDTO toResponse(ChecklistTemplate template) {
        return new ChecklistTemplateResponseDTO(
                template.getId(),
                template.getRoomId(),
                template.getTitle(),
                template.getDescription(),
                template.getVersion(),
                template.getStatus(),
                template.isActive(),
                objectMapper.convertValue(template.getSchemaJson(), ChecklistSchemaDTO.class),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }
}
