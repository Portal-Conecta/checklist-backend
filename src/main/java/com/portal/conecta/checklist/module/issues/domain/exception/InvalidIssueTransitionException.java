package com.portal.conecta.checklist.module.issues.domain.exception;

import com.portal.conecta.checklist.module.issues.domain.enums.IssueStatus;

/**
 * Excecao lancada quando uma transicao de status de issue e invalida.
 *
 * <p>Indica que o status atual da issue nao permite a transicao solicitada.
 * Resulta em HTTP 422 (Unprocessable Entity) via {@code GlobalHandlerException}.</p>
 */
public class InvalidIssueTransitionException extends RuntimeException {

    public InvalidIssueTransitionException(IssueStatus from, IssueStatus to) {
        super("Transicao invalida: nao e possivel mover a pendencia de '%s' para '%s'."
                .formatted(from, to));
    }
}
