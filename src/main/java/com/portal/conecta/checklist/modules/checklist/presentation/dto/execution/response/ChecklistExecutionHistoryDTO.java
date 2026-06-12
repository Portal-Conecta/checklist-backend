package com.portal.conecta.checklist.modules.checklist.presentation.dto.execution.response;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.modules.checklist.domain.enums.Period;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Representa um item do histórico de execuções de checklist de uma turma.
 *
 * @param id identificador único da execução.
 * @param templateId identificador do template usado na execução.
 * @param templateVersion versão do template usado na execução.
 * @param roomId identificador da sala associada à execução.
 * @param classId identificador da turma associada à execução.
 * @param filledBy identificador do usuário que preencheu o checklist.
 * @param period período em que o checklist foi executado.
 * @param checklistType tipo do checklist executado.
 * @param status status atual da execução.
 * @param complianceScore percentual ou pontuação de conformidade calculada para a execução.
 * @param startedAt instante em que a execução foi iniciada.
 * @param submittedAt instante em que a execução foi submetida.
 * @param summary resumo quantitativo das respostas da execução.
 */
public record ChecklistExecutionHistoryDTO(UUID id,
                                           UUID templateId,
                                           Integer templateVersion,
                                           UUID roomId,
                                           UUID classId,
                                           UUID filledBy,
                                           Period period,
                                           ChecklistType checklistType,
                                           ChecklistExecutionStatus status,
                                           BigDecimal complianceScore,
                                           Instant startedAt,
                                           Instant submittedAt,
                                           ChecklistExecutionSummaryDTO summary) {
}
