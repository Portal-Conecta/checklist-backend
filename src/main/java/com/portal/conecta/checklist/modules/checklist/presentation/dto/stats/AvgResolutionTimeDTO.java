package com.portal.conecta.checklist.modules.checklist.presentation.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Tempo médio de resolução de issues.
 *
 * <p>Calculado como {@code avg(EXTRACT(EPOCH FROM (resolved_at - due_at)))} em segundos,
 * somente para issues com {@code resolved_at IS NOT NULL}.</p>
 *
 * @param avgSeconds média em segundos do tempo de resolução
 */
@Schema(description = "Tempo médio de resolução de issues em segundos")
public record AvgResolutionTimeDTO(

        @Schema(description = "Média em segundos do tempo de resolução", example = "86400.0")
        double avgSeconds

) {
}
