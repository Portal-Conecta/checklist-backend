package com.portal.conecta.checklist.shared.hub.client.user;

import com.portal.conecta.checklist.module.checklist.domain.valueobject.UserReference;

import java.util.UUID;

public record HubUserResponse(UUID id, String name, String email, String typeUser, Boolean active) {

    public UserReference toReference(UUID requestedUserId) {
        UUID referenceId = id == null ? requestedUserId : id;
        return new UserReference(referenceId, name, email, typeUser, active);
    }
}
