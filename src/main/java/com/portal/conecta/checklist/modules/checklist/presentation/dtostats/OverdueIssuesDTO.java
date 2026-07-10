package com.portal.conecta.checklist.modules.checklist.presentation.dto.stats;

/**
 * {overdue} — issues vencidas
 */
 */
public record OverdueIssuesDTO(Long overdue) {

    public static OverdueIssuesDTO of(Long overdue) {
        return new OverdueIssuesDTO(overdue);
    }
}