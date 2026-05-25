package com.portal.conecta.checklist.shared.security;

import com.portal.conecta.checklist.shared.context.CurrentUserContext;
import com.portal.conecta.checklist.shared.hub.HubPermissionVersionClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HubPermissionVersionValidator {

    private final HubPermissionVersionClient permissionVersionClient;

    public void validate(CurrentUserContext currentUser) {
        int hubPermissionVersion = permissionVersionClient.getPermissionVersion(currentUser.id());

        if (hubPermissionVersion != currentUser.permissionVersion()) {
            throw new StalePermissionVersionException("User permissions changed. Refresh the access token.");
        }
    }
}
