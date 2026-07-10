package com.portal.conecta.checklist.modules.checklist.presentation.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Média de não-conformidades (issues) por execução de checklist.
 *
 * @param totalIssues      total de issues no período
 * @param totalExecutions  total de execuções distintas com ao menos 1 issue
 * @param avgPerExecution  média: {@code totalIssues / totalExecutions}
 */
@Schema(description = "Média de não-conformidades por execução de checklist")
public record IssuesPerExecutionDTO(

        @Schema(description = "Total de issues", example = "240")
        long totalIssues,

        @Schema(description = "Total de execuções com ao menos 1 issue", example = "80")
        long totalExecutions,

        @Schema(description = "Média de issues por execução", example = "3.00")
        double avgPerExecution

) {
    public static IssuesPerExecutionDTO of(long totalIssues, long totalExecutions) {
        double avg = totalExecutions == 0 ? 0.0
                : Math.round((totalIssues * 100.0 / totalExecutions)) / 100.0;
        return new IssuesPerExecutionDTO(totalIssues, totalExecutions, avg);
    }
}
