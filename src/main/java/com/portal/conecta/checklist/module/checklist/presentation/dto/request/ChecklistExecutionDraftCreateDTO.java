package com.portal.conecta.checklist.module.checklist.presentation.dto.request;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.module.checklist.domain.enums.Period;

import java.util.UUID;

public record ChecklistExecutionDraftCreateDTO(
        UUID templateId,
        UUID roomId,
        UUID classId,
        Period period,
        ChecklistType checklistType
) {}
