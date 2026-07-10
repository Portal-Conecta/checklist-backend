package com.portal.conecta.checklist.modules.checklist.presentation.dto.stats;

/**
 * {submitted, total, ratePercent} com factory of()
 */
 */
public record CompletionRateDTO(Long submitted, Long total, Double ratePercent) {

    public static CompletionRateDTO of(Long submitted, Long total) {
        double rate = total == 0 ? 0.0 : ((double) submitted / total) * 100;
        return new CompletionRateDTO(submitted, total, rate);
    }
}