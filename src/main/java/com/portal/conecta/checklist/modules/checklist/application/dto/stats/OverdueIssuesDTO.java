package com.portal.conecta.checklist.modules.checklist.application.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Total de issues vencidas ({@code due_at < now()} e {@code resolved_at IS NULL}).
 *
 * @param overdue total de issues vencidas e ainda não resolvidas
 */
@Schema(description = "Total de issues vencidas e não resolvidas")
public record OverdueIssuesDTO(

        @Schema(description = "Issues com prazo vencido e sem resolução", example = "12")
        long overdue

) {
}
