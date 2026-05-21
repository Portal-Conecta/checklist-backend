package com.portal.conecta.checklist.shared.security.solid;
import org.springframework.stereotype.Component;

public class DashboardAccessRule implements AuthorizationRule {

    private static final String NAME = "viewDashboard";

    @Override
    public String getName(){
        return NAME;
    }
    @Override
    public boolean isAllowed(PortalUserPrincipal principal){
        if (principal == null)return false;
        if(principal.hasProfile("APRENDIZ")) return false;
        if (principal.hasProfile("ADMINISTRADOR")) return true;
        return principal.hasProfile("PERFIL_SENAI") ||
                principal.hasProfile("PERFIL_WEG");
    }

}
