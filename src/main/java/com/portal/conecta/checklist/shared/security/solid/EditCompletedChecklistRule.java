package com.portal.conecta.checklist.shared.security.solid;

public class EditCompletedChecklistRule implements AuthorizationRule {

    private static final String NAME = "editCompletedChecklist";

    @Override
    public String getName(){
        return NAME;
    }
    @Override
    public boolean isAllowed(PortalUserPrincipal principal){
        if (principal == null) return false;
        if (principal.hasProfile("APRENDIZ")) return false;
        if (principal.hasProfile("ADMINISTRADOR")) return false;
        if (principal.hasProfile("PERFIL_SENAI")
        && "SENAI".equalsIgnoreCase(principal.requestedScope())){
            return true;
        }
        return false;
    }

}
