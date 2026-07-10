package com.portal.conecta.checklist.modules.checklist.application.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Percentual de execuÃ§Ãµes que geraram ao menos 1 nÃ£o-conformidade (issue).
 *
 * @param executionsWithIssues execuÃ§Ãµes distintas com ao menos 1 issue
 * @param totalExecutions      total de execuÃ§Ãµes submetidas
 * @param ratePercent          percentual
 */
@Schema(description = "Percentual de execuÃ§Ãµes com ao menos uma nÃ£o-conformidade")
public record WithIssuesRateDTO(

        @Schema(description = "ExecuÃ§Ãµes com ao menos 1 issue", example = "52")
        long executionsWithIssues,

        @Schema(description = "Total de execuÃ§Ãµes submetidas", example = "128")
        long totalExecutions,

        @Schema(description = "Percentual de execuÃ§Ãµes com issues (0â€“100)", example = "40.63")
        double ratePercent

) {
    public static WithIssuesRateDTO of(long executionsWithIssues, long totalExecutions) {
        double rate = totalExecutions == 0 ? 0.0
                : Math.round((executionsWithIssues * 100.0 / totalExecutions) * 100.0) / 100.0;
        return new WithIssuesRateDTO(executionsWithIssues, totalExecutions, rate);
    }
}
