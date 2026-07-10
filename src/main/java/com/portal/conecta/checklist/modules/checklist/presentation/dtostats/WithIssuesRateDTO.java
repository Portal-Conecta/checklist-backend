package com.portal.conecta.checklist.modules.checklist.presentation.dto.stats;

/**
 * {executionsWithIssues, totalExecutions, ratePercent} com factory of()
 */
 */
public record WithIssuesRateDTO(Long executionsWithIssues, Long totalExecutions, Double ratePercent) {

    public static WithIssuesRateDTO of(Long executionsWithIssues, Long totalExecutions) {
        double rate = totalExecutions == 0 ? 0.0 : ((double) executionsWithIssues / totalExecutions) * 100;
        return new WithIssuesRateDTO(executionsWithIssues, totalExecutions, rate);
    }
}