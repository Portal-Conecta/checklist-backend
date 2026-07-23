package com.portal.conecta.checklist.module.checklist.presentation.dto.execution.response;

import java.util.List;

/**
 * DTO que agrupa respostas de uma execucao de checklist.
 *
 * <p>Combina a lista de respostas com o resumo calculado para facilitar a
 * serializacao do campo JSON de respostas.</p>
 */
public record ChecklistAnswersDTO(
        List<ChecklistAnswerResponseDTO> answers,
        ChecklistExecutionSummaryDTO summary
) {}
