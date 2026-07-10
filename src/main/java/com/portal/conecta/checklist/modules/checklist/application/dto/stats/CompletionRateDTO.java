package com.portal.conecta.checklist.modules.checklist.application.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Taxa de conclusÃ£o de execuÃ§Ãµes de checklist.
 *
 * @param submitted   total de execuÃ§Ãµes com {@code submitted_at} preenchido
 * @param total       total de execuÃ§Ãµes (todos os status, exceto canceladas quando aplicÃ¡vel)
 * @param ratePercent percentual de conclusÃ£o arredondado a duas casas decimais
 */
@Schema(description = "Taxa de conclusÃ£o de execuÃ§Ãµes de checklist")
public record CompletionRateDTO(

        @Schema(description = "ExecuÃ§Ãµes submetidas", example = "128")
        long submitted,

        @Schema(description = "Total de execuÃ§Ãµes consideradas", example = "160")
        long total,

        @Schema(description = "Percentual de conclusÃ£o (0â€“100)", example = "80.00")
        double ratePercent

) {
    /**
     * Calcula o percentual a partir dos totais, evitando divisÃ£o por zero.
     */
    public static CompletionRateDTO of(long submitted, long total) {
        double rate = total == 0 ? 0.0 : Math.round((submitted * 100.0 / total) * 100.0) / 100.0;
        return new CompletionRateDTO(submitted, total, rate);
    }
}
