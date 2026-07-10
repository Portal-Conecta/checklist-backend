package com.portal.conecta.checklist.modules.checklist.application.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Taxa de conclusão de execuções de checklist.
 *
 * @param submitted   total de execuções com {@code submitted_at} preenchido
 * @param total       total de execuções (todos os status, exceto canceladas quando aplicável)
 * @param ratePercent percentual de conclusão arredondado a duas casas decimais
 */
@Schema(description = "Taxa de conclusão de execuções de checklist")
public record CompletionRateDTO(

        @Schema(description = "Execuções submetidas", example = "128")
        long submitted,

        @Schema(description = "Total de execuções consideradas", example = "160")
        long total,

        @Schema(description = "Percentual de conclusão (0–100)", example = "80.00")
        double ratePercent

) {
    /**
     * Calcula o percentual a partir dos totais, evitando divisão por zero.
     */
    public static CompletionRateDTO of(long submitted, long total) {
        double rate = total == 0 ? 0.0 : Math.round((submitted * 100.0 / total) * 100.0) / 100.0;
        return new CompletionRateDTO(submitted, total, rate);
    }
}
