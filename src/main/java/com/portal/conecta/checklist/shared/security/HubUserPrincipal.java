package com.portal.conecta.checklist.shared.security;

import com.portal.conecta.checklist.shared.context.CurrentUserClassLink;

import java.util.List;
import java.util.UUID;

public record HubUserPrincipal(
        UUID id,
        String tokenId,
        String name,
        String email,
        String profile,
        int permissionVersion,
        List<CurrentUserClassLink> classes
) {

    public HubUserPrincipal {
        classes = classes == null ? List.of() : List.copyOf(classes);
    }
}
