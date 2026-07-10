package com.portal.conecta.checklist.modules.checklist.application.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Tempo mÃ©dio de preenchimento de execuÃ§Ãµes agrupado por dia.
 *
 * <p>O tempo Ã© calculado como {@code EXTRACT(EPOCH FROM (submitted_at - started_at))} no
 * Postgres, em segundos. Somente execuÃ§Ãµes com {@code submitted_at IS NOT NULL} sÃ£o consideradas.</p>
 *
 * @param label       data no formato {@code YYYY-MM-DD}
 * @param avgSeconds  mÃ©dia em segundos do tempo de preenchimento naquele dia
 */
@Schema(description = "Tempo mÃ©dio de preenchimento por dia em segundos")
public record AvgFillTimeEntryDTO(

        @Schema(description = "Data no formato ISO YYYY-MM-DD", example = "2026-06-01")
        String label,

        @Schema(description = "MÃ©dia em segundos do tempo de preenchimento", example = "245.5")
        double avgSeconds

) {
}
