package com.portal.conecta.checklist.module.issues.domain.enums;

/**
 * Estados de acompanhamento de uma issue de checklist.
 *
 * <p>Permite controlar o ciclo de vida da pendencia desde a abertura ate o
 * encerramento.</p>
 */
public enum IssueStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    VALIDATED,
    REOPENED,
    CANCELED
}
