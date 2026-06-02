package com.portal.conecta.checklist.shared.hub.exception;

/**
 * Excecao usada quando uma consulta ao Hub falha por indisponibilidade ou erro externo.
 *
 * <p>Diferencia falhas de integracao de erros de regra de negocio do modulo
 * Checklist.</p>
 */
public class HubIntegrationException extends RuntimeException {

    public HubIntegrationException(String message) {
        super(message);
    }

    public HubIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
