package com.portal.conecta.checklist.shared.hub.client.user;

import java.time.Instant;
import java.util.UUID;

public record HubUserResponse(
        UUID id,
        String name,
        String email,
        String typeUser,
        boolean active,
        Instant createdAt
) {
}
