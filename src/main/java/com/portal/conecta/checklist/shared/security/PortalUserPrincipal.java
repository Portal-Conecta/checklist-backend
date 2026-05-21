package com.portal.conecta.checklist.shared.security;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public record PortalUserPrincipal(
        String userId,
        String username,
        Set<String> profiles,
        Long turmaId, // for REPRESENTANTE
        List<Long> linkedTurmaIds, // for DOCENTE
        String scope // e.g. "SENAI" or "WEG"
) {
    public PortalUserPrincipal {
        if (profiles == null) {
            profiles = Collections.emptySet();
        }
        if (linkedTurmaIds == null) {
            linkedTurmaIds = Collections.emptyList();
        }
    }

    public boolean hasProfile(String profile) {
        return profiles.contains(profile);
    }
}
