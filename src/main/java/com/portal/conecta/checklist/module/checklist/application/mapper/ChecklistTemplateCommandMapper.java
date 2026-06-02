package com.portal.conecta.checklist.module.checklist.application.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistTemplateCreateRequest;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Mapper de comandos responsavel por transformar requisicoes de template em
 * entidades persistiveis.
 *
 * <p>Tambem normaliza campos opcionais e converte o schema tipado do request
 * para a estrutura JSON usada no dominio.</p>
 */
@Component
public class ChecklistTemplateCommandMapper {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public ChecklistTemplateCommandMapper(ObjectMapper objectMapper) {
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

    private Map<String, Object> toSchemaJson(ChecklistSchemaDTO schemaJson) {
        if (schemaJson == null) {
            return Map.of();
        }

        return objectMapper.convertValue(schemaJson, MAP_TYPE);
    }

    private String normalizeDescription(String description) {
        return description == null ? "" : description;
    }
}
