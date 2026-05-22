package com.portal.conecta.checklist.module.checklist.presentation.dto.schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ChecklistSchemaDTO(
        @Valid
        @NotEmpty(message = "schemaJson.sections nao pode estar vazio.")
        List<ChecklistSectionDTO> sections
) {}
