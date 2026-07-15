package com.portal.conecta.checklist.modules.checklist.presentation.dto.execution.response;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistCategory;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.modules.checklist.domain.enums.Period;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.shared.ClassResponseDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.shared.RoomResponseDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Representa um item do historico de execucoes de checklist de uma turma.
 */
public record ChecklistExecutionHistoryDTO(UUID id,
                                           UUID templateId,
                                           Integer templateVersion,
                                           UUID roomId,
                                           UUID classId,
                                           UUID filledBy,
                                           Period period,
                                           ChecklistType checklistType,
                                           ChecklistCategory category,
                                           ChecklistExecutionStatus status,
                                           BigDecimal complianceScore,
                                           Instant startedAt,
                                           Instant submittedAt,
                                           ChecklistExecutionSummaryDTO summary,
                                           RoomResponseDTO room,
                                           ClassResponseDTO clazz) {
}
