package com.portal.conecta.checklist.module.checklist.infrastructure.client.hub;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class MockHubRoomClient implements HubRoomClient {

    public static final UUID DEFAULT_ROOM_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final Set<UUID> MOCK_ROOM_IDS = Set.of(
            DEFAULT_ROOM_ID,
            UUID.fromString("11111111-1111-1111-1111-111111111112"),
            UUID.fromString("11111111-1111-1111-1111-111111111113")
    );

    @Override
    public boolean existsById(UUID roomId) {
        return MOCK_ROOM_IDS.contains(roomId);
    }
}
