package com.portal.conecta.checklist.module.checklist.presentation.dto.request;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.module.checklist.domain.enums.Period;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChecklistExecutionDraftCreateDTO(
        @NotNull(message = "templateId e obrigatorio.")
        UUID templateId,

        @NotNull(message = "roomId e obrigatorio.")
        UUID roomId,

        @NotNull(message = "classId e obrigatorio.")
        UUID classId,

        @NotNull(message = "period e obrigatorio.")
        Period period,

        @NotNull(message = "checklistType e obrigatorio.")
        ChecklistType checklistType
) {}
