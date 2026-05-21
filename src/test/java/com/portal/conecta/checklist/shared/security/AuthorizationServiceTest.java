package com.portal.conecta.checklist.shared.security;

import com.portal.conecta.checklist.shared.security.solid.PortalUserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthorizationServiceTest {

    private final AuthorizationService authService = new AuthorizationService();
    private SecurityContext originalContext;

    @BeforeEach
    void setUp() {
        originalContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(mock(SecurityContext.class));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(originalContext);
    }

    private void mockAuthentication(PortalUserPrincipal principal) {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(auth);
    }

    @Test
    @DisplayName("should deny operational access for APRENDIZ")
    void shouldDenyOperationalAccessForAprendiz() {
        PortalUserPrincipal principal = new PortalUserPrincipal(
                "1", "Aprendiz User", Set.of("APRENDIZ"), null, Collections.emptyList(), null
        );
        mockAuthentication(principal);

        assertFalse(authService.hasOperationalAccess());
    }

    @Test
    @DisplayName("should allow operational access for non-APRENDIZ profiles")
    void shouldAllowOperationalAccessForNonAprendiz() {
        List<String> operationalProfiles = List.of("REPRESENTANTE", "DOCENTE", "PERFIL_SENAI", "PERFIL_WEG", "ADMINISTRADOR");

        for (String profile : operationalProfiles) {
            PortalUserPrincipal principal = new PortalUserPrincipal(
                    "1", "User " + profile, Set.of(profile), null, Collections.emptyList(), null
            );
            mockAuthentication(principal);
            assertTrue(authService.hasOperationalAccess(), "Failed for profile: " + profile);
        }
    }

    @Test
    @DisplayName("REPRESENTANTE can create execution only for their own class")
    void representanteCanCreateExecutionOnlyForOwnClass() {
        PortalUserPrincipal principal = new PortalUserPrincipal(
                "1", "Rep User", Set.of("REPRESENTANTE"), 100L, Collections.emptyList(), null
        );
        mockAuthentication(principal);

        assertTrue(authService.canCreateExecution(100L));
        assertFalse(authService.canCreateExecution(101L));
        assertFalse(authService.canCreateExecution(null));
    }

    @Test
    @DisplayName("DOCENTE can create execution only for linked classes")
    void docenteCanCreateExecutionOnlyForLinkedClasses() {
        PortalUserPrincipal principal = new PortalUserPrincipal(
                "1", "Docente User", Set.of("DOCENTE"), null, List.of(200L, 201L), null
        );
        mockAuthentication(principal);

        assertTrue(authService.canCreateExecution(200L));
        assertTrue(authService.canCreateExecution(201L));
        assertFalse(authService.canCreateExecution(202L));
        assertFalse(authService.canCreateExecution(null));
    }

    @Test
    @DisplayName("ADMINISTRADOR can create execution for any class")
    void administradorCanCreateExecutionForAnyClass() {
        PortalUserPrincipal principal = new PortalUserPrincipal(
                "1", "Admin User", Set.of("ADMINISTRADOR"), null, Collections.emptyList(), null
        );
        mockAuthentication(principal);

        assertTrue(authService.canCreateExecution(999L));
    }

    @Test
    @DisplayName("APRENDIZ is blocked from creating execution even if matching class")
    void aprendizBlockedFromCreatingExecution() {
        PortalUserPrincipal principal = new PortalUserPrincipal(
                "1", "Aprendiz User", Set.of("APRENDIZ"), 100L, List.of(100L), null
        );
        mockAuthentication(principal);

        assertFalse(authService.canCreateExecution(100L));
    }

    @Test
    @DisplayName("should allow dashboard access only for PERFIL_SENAI, PERFIL_WEG, and ADMINISTRADOR")
    void shouldAllowDashboardAccess() {
        // Allowed profiles
        List<String> allowed = List.of("PERFIL_SENAI", "PERFIL_WEG", "ADMINISTRADOR");
        for (String profile : allowed) {
            PortalUserPrincipal principal = new PortalUserPrincipal(
                    "1", "User " + profile, Set.of(profile), null, Collections.emptyList(), null
            );
            mockAuthentication(principal);
            assertTrue(authService.canViewDashboard(), "Failed to allow for: " + profile);
        }

        // Denied profiles
        List<String> denied = List.of("REPRESENTANTE", "DOCENTE", "APRENDIZ");
        for (String profile : denied) {
            PortalUserPrincipal principal = new PortalUserPrincipal(
                    "1", "User " + profile, Set.of(profile), null, Collections.emptyList(), null
            );
            mockAuthentication(principal);
            assertFalse(authService.canViewDashboard(), "Failed to deny for: " + profile);
        }
    }

    @Test
    @DisplayName("should enforce scope rules for editing completed checklists")
    void shouldEnforceScopeRulesForEditingCompletedChecklists() {
        // SENAI profile
        PortalUserPrincipal senaiUser = new PortalUserPrincipal(
                "1", "SENAI User", Set.of("PERFIL_SENAI"), null, Collections.emptyList(), "SENAI"
        );
        mockAuthentication(senaiUser);
        assertTrue(authService.canEditCompletedChecklist("SENAI"));
        assertFalse(authService.canEditCompletedChecklist("WEG"));

        // WEG profile
        PortalUserPrincipal wegUser = new PortalUserPrincipal(
                "2", "WEG User", Set.of("PERFIL_WEG"), null, Collections.emptyList(), "WEG"
        );
        mockAuthentication(wegUser);
        assertTrue(authService.canEditCompletedChecklist("WEG"));
        assertFalse(authService.canEditCompletedChecklist("SENAI"));

        // Admin profile
        PortalUserPrincipal adminUser = new PortalUserPrincipal(
                "3", "Admin User", Set.of("ADMINISTRADOR"), null, Collections.emptyList(), null
        );
        mockAuthentication(adminUser);
        assertTrue(authService.canEditCompletedChecklist("SENAI"));
        assertTrue(authService.canEditCompletedChecklist("WEG"));

        // Aprendiz profile
        PortalUserPrincipal aprendizUser = new PortalUserPrincipal(
                "4", "Aprendiz User", Set.of("APRENDIZ"), null, Collections.emptyList(), "SENAI"
        );
        mockAuthentication(aprendizUser);
        assertFalse(authService.canEditCompletedChecklist("SENAI"));
    }
}
