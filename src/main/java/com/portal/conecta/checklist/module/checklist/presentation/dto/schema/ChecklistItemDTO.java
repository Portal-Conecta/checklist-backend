package com.portal.conecta.checklist.module.checklist.presentation.dto.schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChecklistItemDTO(
        @NotBlank(message = "item.key e obrigatorio.")
        String key,

        @NotBlank(message = "item.title e obrigatorio.")
        @Size(max = 150, message = "item.title deve ter no maximo 150 caracteres.")
        String title,

        @Size(max = 250, message = "item.description deve ter no maximo 250 caracteres.")
        String description,

        @NotNull(message = "item.required e obrigatorio.")
        Boolean required,

        @NotNull(message = "item.order e obrigatorio.")
        Integer order
) {}
