package com.portal.conecta.checklist.shared.security.solid;

public class OperationalAccessRule implements AuthorizationRule{
    private static final String NAME = "operationalAccess";

    @Override
    public String getName(){
        return NAME;
    }
    @Override
    public boolean isAllowed(PortalUserPrincipal principal){
        return principal != null &&
                !principal.hasProfile("APRENDIZ");
    }

}
