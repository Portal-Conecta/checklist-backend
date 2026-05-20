package com.portal.conecta.checklist.module.issues.presentation.dto.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estado atual da pendência")
public enum IssueStatus {

    @Schema(description = "Pendência registrada, sem responsável atuando")
    OPEN,

    @Schema(description = "Responsável está atuando na resolução")
    IN_PROGRESS,

    @Schema(description = "Responsável sinalizou como resolvida")
    RESOLVED,

    @Schema(description = "Gestor validou a resolução")
    VALIDATED,

    @Schema(description = "Gestor reabriu a pendência")
    REOPENED,

    @Schema(description = "Pendência cancelada sem resolução")
    CANCELED
}
