package com.portal.conecta.checklist.shared.hub;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile({"mock", "test"})
public class MockHubPermissionVersionClient implements HubPermissionVersionClient {

    private final int permissionVersion;

    public MockHubPermissionVersionClient(
            @Value("${checklist.mock.current-user.permission-version:1}") int permissionVersion
    ) {
        this.permissionVersion = permissionVersion;
    }

    @Override
    public int getPermissionVersion(UUID userId) {
        return permissionVersion;
    }
}
