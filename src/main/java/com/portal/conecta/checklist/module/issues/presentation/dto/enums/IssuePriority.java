package com.portal.conecta.checklist.module.issues.presentation.dto.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Prioridade da pendência")
public enum IssuePriority {

    @Schema(description = "Baixa prioridade")
    LOW,

    @Schema(description = "Prioridade média")
    MEDIUM,

    @Schema(description = "Alta prioridade")
    HIGH,

    @Schema(description = "Crítico — requer atenção imediata")
    CRITICAL
}
