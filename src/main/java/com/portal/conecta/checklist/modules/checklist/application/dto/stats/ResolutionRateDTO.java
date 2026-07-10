package com.portal.conecta.checklist.modules.checklist.application.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Taxa de resolução de issues (resolvidas / total).
 *
 * @param resolved    total de issues resolvidas
 * @param total       total de issues
 * @param ratePercent percentual de resolução
 */
@Schema(description = "Taxa de resolução de issues")
public record ResolutionRateDTO(

        @Schema(description = "Issues resolvidas", example = "83")
        long resolved,

        @Schema(description = "Total de issues", example = "128")
        long total,

        @Schema(description = "Percentual de resolução (0–100)", example = "64.84")
        double ratePercent

) {
    public static ResolutionRateDTO of(long resolved, long total) {
        double rate = total == 0 ? 0.0 : Math.round((resolved * 100.0 / total) * 100.0) / 100.0;
        return new ResolutionRateDTO(resolved, total, rate);
    }
}
