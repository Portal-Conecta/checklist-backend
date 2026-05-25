package com.portal.conecta.checklist.shared.security;

public class StalePermissionVersionException extends RuntimeException {

    public StalePermissionVersionException(String message) {
        super(message);
    }
}
