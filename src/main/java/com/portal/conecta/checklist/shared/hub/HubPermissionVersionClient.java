package com.portal.conecta.checklist.shared.hub;

import java.util.UUID;

public interface HubPermissionVersionClient {

    int getPermissionVersion(UUID userId);
}
