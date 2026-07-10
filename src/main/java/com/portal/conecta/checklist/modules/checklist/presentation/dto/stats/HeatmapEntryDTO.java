package com.portal.conecta.checklist.modules.checklist.presentation.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Célula do heatmap turno × dia da semana.
 *
 * <p>Permite construir uma tabela bidimensional no front onde cada célula exibe
 * quantas execuções ocorreram para aquele turno naquele dia da semana.</p>
 *
 * @param shift     turno (ex: {@code MORNING}, {@code AFTERNOON}, {@code NIGHT})
 * @param dayOfWeek dia da semana como número ISO (0 = domingo … 6 = sábado, conforme {@code EXTRACT(DOW)})
 * @param count     total de execuções na interseção
 */
@Schema(description = "Célula do heatmap de execuções por turno e dia da semana")
public record HeatmapEntryDTO(

        @Schema(description = "Turno da execução", example = "MORNING")
        String shift,

        @Schema(description = "Dia da semana (0=domingo, 1=segunda … 6=sábado)", example = "1")
        int dayOfWeek,

        @Schema(description = "Total de execuções na interseção turno × dia", example = "34")
        long count

) {
}
