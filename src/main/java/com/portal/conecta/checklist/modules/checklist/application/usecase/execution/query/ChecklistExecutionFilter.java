package com.portal.conecta.checklist.modules.checklist.application.usecase.execution.query;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChecklistExecutionFilter(
        UUID classId,
        UUID roomId,
        LocalDateTime from,
        LocalDateTime to
) {}
