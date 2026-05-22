package com.portal.conecta.checklist.module.checklist.presentation.dto.schema;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

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
        List<ChecklistItemDTO> items
) {}
