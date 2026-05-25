package com.portal.conecta.checklist.shared.hub;

public class HubIntegrationException extends RuntimeException {

    public HubIntegrationException(String message) {
        super(message);
    }

    public HubIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
