package com.portal.conecta.checklist.modules.checklist.application.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Tempo mÃ©dio de resoluÃ§Ã£o de issues.
 *
 * <p>Calculado como {@code avg(EXTRACT(EPOCH FROM (resolved_at - due_at)))} em segundos,
 * somente para issues com {@code resolved_at IS NOT NULL}.</p>
 *
 * @param avgSeconds mÃ©dia em segundos do tempo de resoluÃ§Ã£o
 */
@Schema(description = "Tempo mÃ©dio de resoluÃ§Ã£o de issues em segundos")
public record AvgResolutionTimeDTO(

        @Schema(description = "MÃ©dia em segundos do tempo de resoluÃ§Ã£o", example = "86400.0")
        double avgSeconds

) {
}
