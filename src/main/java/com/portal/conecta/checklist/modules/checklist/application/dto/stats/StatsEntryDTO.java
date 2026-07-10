package com.portal.conecta.checklist.modules.checklist.application.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * ProjeÃ§Ã£o genÃ©rica "categoria â†’ valor" pronta para consumo pelo Chart.js.
 *
 * <p>Serve para grÃ¡ficos de barra, pizza e linha. Quando a mÃ©trica Ã© uma sÃ©rie
 * temporal, {@code label} contÃ©m a data no formato {@code YYYY-MM-DD}.</p>
 *
 * @param label rÃ³tulo da categoria (status, tipo, data ISO, etc.)
 * @param value contagem ou valor agregado correspondente
 */
@Schema(description = "Entrada genÃ©rica de estatÃ­stica no formato {label, value} para Chart.js")
public record StatsEntryDTO(

        @Schema(description = "RÃ³tulo da categoria (ex: 'SUBMITTED', '2026-06-01')", example = "SUBMITTED")
        String label,

        @Schema(description = "Valor agregado (contagem, mÃ©dia, etc.)", example = "128")
        long value

) {
}
