package com.portal.conecta.checklist.module.checklist.presentation.dto.response;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.module.checklist.domain.enums.Period;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ChecklistExecutionHistoryDTO(UUID id,
                                           UUID templateId,
                                           Integer templateVersion,
                                           UUID roomId,
                                           UUID classId,
                                           UUID filledBy,
                                           Period period,
                                           ChecklistType checklistType,
                                           ChecklistExecutionStatus status,
                                           BigDecimal compliaceScore,
                                           Instant startedAt,
                                           Instant submittedAt,
                                           ChecklistExecutionSummaryDTO summary) {
}
