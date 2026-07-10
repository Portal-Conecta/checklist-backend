package com.portal.conecta.checklist.modules.checklist.application.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Tempo médio de preenchimento de execuções agrupado por dia.
 *
 * <p>O tempo é calculado como {@code EXTRACT(EPOCH FROM (submitted_at - started_at))} no
 * Postgres, em segundos. Somente execuções com {@code submitted_at IS NOT NULL} são consideradas.</p>
 *
 * @param label       data no formato {@code YYYY-MM-DD}
 * @param avgSeconds  média em segundos do tempo de preenchimento naquele dia
 */
@Schema(description = "Tempo médio de preenchimento por dia em segundos")
public record AvgFillTimeEntryDTO(

        @Schema(description = "Data no formato ISO YYYY-MM-DD", example = "2026-06-01")
        String label,

        @Schema(description = "Média em segundos do tempo de preenchimento", example = "245.5")
        double avgSeconds

) {
}
