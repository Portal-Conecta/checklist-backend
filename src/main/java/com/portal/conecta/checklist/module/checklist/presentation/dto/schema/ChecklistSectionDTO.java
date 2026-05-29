package com.portal.conecta.checklist.module.checklist.presentation.dto.schema;


import com.portal.conecta.checklist.module.checklist.domain.validation.ChecklistTemplateLimits;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ChecklistSectionDTO(
        @NotBlank(message = "section.key e obrigatorio.")
        String key,

        @NotBlank(message = "section.title e obrigatorio.")
        String title,

        @NotNull(message = "section.order e obrigatorio.")
        Integer order,

        @Valid
        @NotEmpty(message = "section.items nao pode estar vazio.")
        @Size(max = ChecklistTemplateLimits.MAX_ITEMS_PER_SECTION, message = "section.items deve ter no maximo 50 itens.")
        @ArraySchema(
                minItems = 1,
                maxItems = ChecklistTemplateLimits.MAX_ITEMS_PER_SECTION,
                schema = @Schema(implementation = ChecklistItemDTO.class),
                arraySchema = @Schema(description = "Itens da secao. Maximo de 50 itens por secao.")
        )
        List<ChecklistItemDTO> items
) {}
