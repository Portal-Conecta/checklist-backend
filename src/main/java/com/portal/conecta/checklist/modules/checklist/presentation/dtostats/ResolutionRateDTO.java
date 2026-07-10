package com.portal.conecta.checklist.modules.checklist.presentation.dto.stats;

/**
 * {resolved, total, ratePercent} com factory of()
 */
 */
public record ResolutionRateDTO(Long resolved, Long total, Double ratePercent) {

    public static ResolutionRateDTO of(Long resolved, Long total) {
        double rate = total == 0 ? 0.0 : ((double) resolved / total) * 100;
        return new ResolutionRateDTO(resolved, total, rate);
    }
}