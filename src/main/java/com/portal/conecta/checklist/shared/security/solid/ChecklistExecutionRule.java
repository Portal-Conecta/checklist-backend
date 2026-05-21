package com.portal.conecta.checklist.shared.security.solid;
import org.springframework.stereotype.Component;

public class ChecklistExecutionRule implements AuthorizationRule {
    private static final String NAME = "createExecution";

    @Override
    public String getName(){
        return NAME;
    }
    @Override
    public boolean isAllowed(PortalUserPrincipal principal){
        if (principal == null) return false;

        if (principal.hasProfile("APRENDIZ")) return false;
        if (principal.hasProfile("ADMINISTRADOR")) return true;
        if (principal.hasProfile("REPRESENTANTE")){
            return principal.turmaId() !=null && principal.turmaId().equals(principal.requestedTurmasId());
        }
        if (principal.hasProfile("DOCENTE")){
            return principal.linkedTurmaIds() != null && principal.linkedTurmaIds().contains(principal.requestedTurmasId());
        }
        return false;
    }
}
