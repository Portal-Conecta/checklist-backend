package com.portal.conecta.checklist.shared.security.solid;

public interface AuthorizationRule {
    String getName();
    boolean isAllowed(PortalUserPrincipal principal);
}
