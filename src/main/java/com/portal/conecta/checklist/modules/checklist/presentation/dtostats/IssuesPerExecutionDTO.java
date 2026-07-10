package com.portal.conecta.checklist.modules.checklist.presentation.dto.stats;

/**
 * {totalIssues, totalExecutions, avgPerExecution} com factory of()
 */
 */
public record IssuesPerExecutionDTO(Long totalIssues, Long totalExecutions, Double avgPerExecution) {

    public static IssuesPerExecutionDTO of(Long totalIssues, Long totalExecutions) {
        double avg = totalExecutions == 0 ? 0.0 : (double) totalIssues / totalExecutions;
        return new IssuesPerExecutionDTO(totalIssues, totalExecutions, avg);
    }
}