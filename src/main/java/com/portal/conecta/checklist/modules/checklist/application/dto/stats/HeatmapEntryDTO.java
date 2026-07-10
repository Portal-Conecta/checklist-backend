package com.portal.conecta.checklist.modules.checklist.application.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * CÃ©lula do heatmap turno Ã— dia da semana.
 *
 * <p>Permite construir uma tabela bidimensional no front onde cada cÃ©lula exibe
 * quantas execuÃ§Ãµes ocorreram para aquele turno naquele dia da semana.</p>
 *
 * @param shift     turno (ex: {@code MORNING}, {@code AFTERNOON}, {@code NIGHT})
 * @param dayOfWeek dia da semana como nÃºmero ISO (0 = domingo â€¦ 6 = sÃ¡bado, conforme {@code EXTRACT(DOW)})
 * @param count     total de execuÃ§Ãµes na interseÃ§Ã£o
 */
@Schema(description = "CÃ©lula do heatmap de execuÃ§Ãµes por turno e dia da semana")
public record HeatmapEntryDTO(

        @Schema(description = "Turno da execuÃ§Ã£o", example = "MORNING")
        String shift,

        @Schema(description = "Dia da semana (0=domingo, 1=segunda â€¦ 6=sÃ¡bado)", example = "1")
        int dayOfWeek,

        @Schema(description = "Total de execuÃ§Ãµes na interseÃ§Ã£o turno Ã— dia", example = "34")
        long count

) {
}
