package com.portal.conecta.checklist.module.checklist.presentation.dto.schema;

import java.util.List;

public record ChecklistSchemaDTO(
        List<ChecklistSectionDTO> sections
) {}