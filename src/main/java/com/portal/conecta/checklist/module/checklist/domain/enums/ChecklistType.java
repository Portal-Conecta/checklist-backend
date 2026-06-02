package com.portal.conecta.checklist.module.checklist.domain.enums;

/**
 * Tipo funcional da execucao de checklist.
 *
 * <p>Diferencia fluxos de chegada e saida para permitir regras especificas
 * de negocio, como dependencia entre execucoes.</p>
 */
public enum ChecklistType {
    ARRIVAL,
    POST_BREAK
}
