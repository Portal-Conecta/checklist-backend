package com.portal.conecta.checklist.modules.checklist.presentation.dto.execution.response;

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
 *
 * @param id identificador unico da execucao.
 * @param templateId identificador do template usado na execucao.
 * @param templateVersion versao do template usado na execucao.
 * @param roomId identificador da sala associada a execucao.
 * @param classId identificador da turma associada a execucao.
 * @param filledBy identificador do usuario que criou o rascunho.
 * @param submittedBy identificador de quem submeteu (nullable).
 * @param canceledBy identificador de quem cancelou (nullable).
 * @param period periodo em que o checklist foi executado.
 * @param checklistType tipo do checklist executado.
 * @param status status atual da execucao.
 * @param complianceScore percentual ou pontuacao de conformidade calculada para a execucao.
 * @param startedAt instante em que a execucao foi iniciada.
 * @param submittedAt instante em que a execucao foi submetida.
 * @param summary resumo quantitativo das respostas da execucao.
 * @param room dados enriquecidos da sala.
 * @param clazz dados enriquecidos da turma.
 */
public record ChecklistExecutionHistoryDTO(UUID id,
                                           UUID templateId,
                                           Integer templateVersion,
                                           UUID roomId,
                                           UUID classId,
                                           UUID filledBy,
                                           UUID submittedBy,
                                           UUID canceledBy,
                                           Period period,
                                           ChecklistType checklistType,
                                           ChecklistExecutionStatus status,
                                           BigDecimal complianceScore,
                                           Instant startedAt,
                                           Instant submittedAt,
                                           ChecklistExecutionSummaryDTO summary,
                                           RoomResponseDTO room,
                                           ClassResponseDTO clazz) {
}
