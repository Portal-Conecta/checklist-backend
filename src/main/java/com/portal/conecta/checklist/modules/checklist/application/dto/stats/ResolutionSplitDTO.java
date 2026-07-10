package com.portal.conecta.checklist.modules.checklist.application.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DivisÃ£o de issues entre abertas (sem {@code resolved_at}) e resolvidas.
 *
 * @param open     total de issues com {@code resolved_at IS NULL}
 * @param resolved total de issues com {@code resolved_at IS NOT NULL}
 * @param total    soma de abertas e resolvidas
 */
@Schema(description = "DivisÃ£o entre issues abertas e resolvidas")
public record ResolutionSplitDTO(

        @Schema(description = "Issues sem data de resoluÃ§Ã£o", example = "45")
        long open,

        @Schema(description = "Issues com data de resoluÃ§Ã£o", example = "83")
        long resolved,

        @Schema(description = "Total de issues", example = "128")
        long total

) {
    public static ResolutionSplitDTO of(long open, long resolved) {
        return new ResolutionSplitDTO(open, resolved, open + resolved);
    }
}
