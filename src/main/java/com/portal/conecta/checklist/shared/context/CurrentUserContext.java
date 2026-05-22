package com.portal.conecta.checklist.shared.context;

import java.util.UUID;

public record CurrentUserContext(
        UUID id,
        String name,
        String email,
        String profile
) {

    public boolean canManageChecklistTemplates() {
        return "PERFIL_SENAI".equals(profile) || "PERFIL_WEG".equals(profile);
    }
}
