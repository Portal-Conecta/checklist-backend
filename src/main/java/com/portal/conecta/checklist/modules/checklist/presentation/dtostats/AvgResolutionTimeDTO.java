package com.portal.conecta.checklist.modules.checklist.presentation.dto.stats;

/**
 * {avgSeconds} — tempo médio de resolução de issues
 */
public record AvgResolutionTimeDTO(Double avgSeconds) {

    public static AvgResolutionTimeDTO of(Double avgSeconds) {
        return new AvgResolutionTimeDTO(avgSeconds);
    }
}