package com.portal.conecta.checklist.modules.checklist.application.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Percentual de execuções que geraram ao menos 1 não-conformidade (issue).
 *
 * @param executionsWithIssues execuções distintas com ao menos 1 issue
 * @param totalExecutions      total de execuções submetidas
 * @param ratePercent          percentual
 */
@Schema(description = "Percentual de execuções com ao menos uma não-conformidade")
public record WithIssuesRateDTO(

        @Schema(description = "Execuções com ao menos 1 issue", example = "52")
        long executionsWithIssues,

        @Schema(description = "Total de execuções submetidas", example = "128")
        long totalExecutions,

        @Schema(description = "Percentual de execuções com issues (0–100)", example = "40.63")
        double ratePercent

) {
    public static WithIssuesRateDTO of(long executionsWithIssues, long totalExecutions) {
        double rate = totalExecutions == 0 ? 0.0
                : Math.round((executionsWithIssues * 100.0 / totalExecutions) * 100.0) / 100.0;
        return new WithIssuesRateDTO(executionsWithIssues, totalExecutions, rate);
    }
}
