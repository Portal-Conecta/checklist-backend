package com.portal.conecta.checklist.module.checklist.presentation.dto.schema;

import com.portal.conecta.checklist.module.checklist.domain.validation.ChecklistTemplateLimits;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ChecklistSchemaDTO(
        @Valid
        @NotEmpty(message = "schemaJson.sections nao pode estar vazio.")
        @Size(max = ChecklistTemplateLimits.MAX_SECTIONS, message = "schemaJson.sections deve ter no maximo 20 secoes.")
        @ArraySchema(
                minItems = 1,
                maxItems = ChecklistTemplateLimits.MAX_SECTIONS,
                schema = @Schema(implementation = ChecklistSectionDTO.class),
                arraySchema = @Schema(description = "Secoes do template. Maximo de 20 secoes por template.")
        )
        List<ChecklistSectionDTO> sections
) {}
