package com.portal.conecta.checklist.modules.checklist.application.usecase.execution.query;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistCategory;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChecklistExecutionFilter(
        UUID classId,
        UUID roomId,
        ChecklistCategory category,
        LocalDateTime from,
        LocalDateTime to
) {}
