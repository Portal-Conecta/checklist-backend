package com.portal.conecta.checklist.shared.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("authService")
public class AuthorizationService {

    public PortalUserPrincipal getPrincipal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof PortalUserPrincipal principal) {
            return principal;
        }
        return null;
    }

    /**
     * Blocks operational access for APRENDIZ.
     * All operational endpoints/actions should require this check.
     */
    public boolean hasOperationalAccess() {
        PortalUserPrincipal principal = getPrincipal();
        if (principal == null) {
            return false;
        }
        // Block operational access for APRENDIZ
        return !principal.hasProfile("APRENDIZ");
    }

    /**
     * Checks if the user can create a checklist execution for the given class (turma).
     * - REPRESENTANTE: allowed only for their own class.
     * - DOCENTE: allowed only for linked classes.
     * - ADMINISTRADOR: allowed for administration / operational actions (or if they have the class).
     * - APRENDIZ: blocked.
     */
    public boolean canCreateExecution(Long turmaId) {
        PortalUserPrincipal principal = getPrincipal();
        if (principal == null || turmaId == null) {
            return false;
        }

        // Block operational access for APRENDIZ
        if (principal.hasProfile("APRENDIZ")) {
            return false;
        }

        // ADMINISTRADOR has general access
        if (principal.hasProfile("ADMINISTRADOR")) {
            return true;
        }

        // REPRESENTANTE: own class only
        if (principal.hasProfile("REPRESENTANTE")) {
            return principal.turmaId() != null && principal.turmaId().equals(turmaId);
        }

        // DOCENTE: linked classes only
        if (principal.hasProfile("DOCENTE")) {
            return principal.linkedTurmaIds() != null && principal.linkedTurmaIds().contains(turmaId);
        }

        return false;
    }

    /**
     * Checks if the user can access the dashboard.
     * - PERFIL_SENAI and PERFIL_WEG: allowed.
     * - ADMINISTRADOR: allowed.
     * - APRENDIZ: blocked.
     */
    public boolean canViewDashboard() {
        PortalUserPrincipal principal = getPrincipal();
        if (principal == null) {
            return false;
        }

        if (principal.hasProfile("APRENDIZ")) {
            return false;
        }

        if (principal.hasProfile("ADMINISTRADOR")) {
            return true;
        }

        return principal.hasProfile("PERFIL_SENAI") || principal.hasProfile("PERFIL_WEG");
    }

    /**
     * Checks if the user can edit a completed checklist.
     * - PERFIL_SENAI: allowed only for "SENAI" scope.
     * - PERFIL_WEG: allowed only for "WEG" scope.
     * - ADMINISTRADOR: allowed.
     * - APRENDIZ: blocked.
     */
    public boolean canEditCompletedChecklist(String targetScope) {
        PortalUserPrincipal principal = getPrincipal();
        if (principal == null || targetScope == null) {
            return false;
        }

        if (principal.hasProfile("APRENDIZ")) {
            return false;
        }

        if (principal.hasProfile("ADMINISTRADOR")) {
            return true;
        }

        if (principal.hasProfile("PERFIL_SENAI") && "SENAI".equalsIgnoreCase(targetScope)) {
            return true;
        }

        if (principal.hasProfile("PERFIL_WEG") && "WEG".equalsIgnoreCase(targetScope)) {
            return true;
        }

        return false;
    }
}
