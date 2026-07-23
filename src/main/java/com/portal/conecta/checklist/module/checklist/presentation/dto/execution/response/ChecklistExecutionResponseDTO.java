package com.portal.conecta.checklist.module.checklist.presentation.dto.execution.response;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistCategory;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.module.checklist.domain.enums.Period;
import com.portal.conecta.checklist.module.checklist.presentation.dto.shared.RoomResponseDTO;
import com.portal.conecta.checklist.module.issues.presentation.dto.response.ChecklistIssueResponseDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO de resposta para execucoes de checklist.
 *
 * <p>Consolida os dados de identificacao, status, respostas e score que a API
 * devolve para clientes apos operacoes de rascunho, submit ou cancelamento.</p>
 */
public record ChecklistExecutionResponseDTO(
        UUID id,
        UUID templateId,
        Integer templateVersion,
        UUID roomId,
        UUID classId,
        UUID filledBy,
        UUID submittedBy,
        UUID canceledBy,
        Period period,
        ChecklistType checklistType,
        ChecklistCategory category,
        ChecklistExecutionStatus status,
        BigDecimal complianceScore,
        ChecklistAnswersDTO answersJson,
        ChecklistExecutionSummaryDTO summary,
        Instant startedAt,
        Instant submittedAt,
        List<ChecklistIssueResponseDTO> issues,
        RoomResponseDTO room
) {}
