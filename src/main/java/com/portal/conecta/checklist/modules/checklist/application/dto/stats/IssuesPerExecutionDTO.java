package com.portal.conecta.checklist.modules.checklist.application.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * MÃ©dia de nÃ£o-conformidades (issues) por execuÃ§Ã£o de checklist.
 *
 * @param totalIssues      total de issues no perÃ­odo
 * @param totalExecutions  total de execuÃ§Ãµes distintas com ao menos 1 issue
 * @param avgPerExecution  mÃ©dia: {@code totalIssues / totalExecutions}
 */
@Schema(description = "MÃ©dia de nÃ£o-conformidades por execuÃ§Ã£o de checklist")
public record IssuesPerExecutionDTO(

        @Schema(description = "Total de issues", example = "240")
        long totalIssues,

        @Schema(description = "Total de execuÃ§Ãµes com ao menos 1 issue", example = "80")
        long totalExecutions,

        @Schema(description = "MÃ©dia de issues por execuÃ§Ã£o", example = "3.00")
        double avgPerExecution

) {
    public static IssuesPerExecutionDTO of(long totalIssues, long totalExecutions) {
        double avg = totalExecutions == 0 ? 0.0
                : Math.round((totalIssues * 100.0 / totalExecutions)) / 100.0;
        return new IssuesPerExecutionDTO(totalIssues, totalExecutions, avg);
    }
}
