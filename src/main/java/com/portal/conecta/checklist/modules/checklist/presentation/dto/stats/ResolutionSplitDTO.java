package com.portal.conecta.checklist.modules.checklist.presentation.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Divisão de issues entre abertas (sem {@code resolved_at}) e resolvidas.
 *
 * @param open     total de issues com {@code resolved_at IS NULL}
 * @param resolved total de issues com {@code resolved_at IS NOT NULL}
 * @param total    soma de abertas e resolvidas
 */
@Schema(description = "Divisão entre issues abertas e resolvidas")
public record ResolutionSplitDTO(

        @Schema(description = "Issues sem data de resolução", example = "45")
        long open,

        @Schema(description = "Issues com data de resolução", example = "83")
        long resolved,

        @Schema(description = "Total de issues", example = "128")
        long total

) {
    public static ResolutionSplitDTO of(long open, long resolved) {
        return new ResolutionSplitDTO(open, resolved, open + resolved);
    }
}
