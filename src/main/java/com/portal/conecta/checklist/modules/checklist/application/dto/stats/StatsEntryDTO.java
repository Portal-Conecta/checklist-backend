package com.portal.conecta.checklist.modules.checklist.application.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Projeção genérica "categoria → valor" pronta para consumo pelo Chart.js.
 *
 * <p>Serve para gráficos de barra, pizza e linha. Quando a métrica é uma série
 * temporal, {@code label} contém a data no formato {@code YYYY-MM-DD}.</p>
 *
 * @param label rótulo da categoria (status, tipo, data ISO, etc.)
 * @param value contagem ou valor agregado correspondente
 */
@Schema(description = "Entrada genérica de estatística no formato {label, value} para Chart.js")
public record StatsEntryDTO(

        @Schema(description = "Rótulo da categoria (ex: 'SUBMITTED', '2026-06-01')", example = "SUBMITTED")
        String label,

        @Schema(description = "Valor agregado (contagem, média ou percentual)", example = "128")
        Number value

) {
}
