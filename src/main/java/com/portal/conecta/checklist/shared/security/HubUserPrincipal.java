package com.portal.conecta.checklist.shared.security;

import com.portal.conecta.checklist.shared.context.CurrentUserClassLink;

import java.util.List;
import java.util.UUID;

public record HubUserPrincipal(
        UUID id,
        String name,
        String email,
        String profile,
        List<CurrentUserClassLink> classes
) {

    public HubUserPrincipal {
        classes = classes == null ? List.of() : List.copyOf(classes);
    }
}
