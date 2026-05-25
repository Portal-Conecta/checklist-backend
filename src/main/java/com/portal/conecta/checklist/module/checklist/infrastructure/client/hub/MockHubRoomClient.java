package com.portal.conecta.checklist.module.checklist.infrastructure.client.hub;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Profile({"mock", "test"})
public class MockHubRoomClient implements HubRoomClient {

    public static final UUID DEFAULT_ROOM_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final String DEFAULT_ROOM_IDS = "11111111-1111-1111-1111-111111111111,"
            + "11111111-1111-1111-1111-111111111112,"
            + "11111111-1111-1111-1111-111111111113";

    private final Set<UUID> mockRoomIds;

    public MockHubRoomClient(@Value("${checklist.mock.hub.room-ids:" + DEFAULT_ROOM_IDS + "}") String roomIds) {
        this.mockRoomIds = parseRoomIds(roomIds);
    }

    @Override
    public boolean existsById(UUID roomId) {
        return mockRoomIds.contains(roomId);
    }

    private Set<UUID> parseRoomIds(String roomIds) {
        return Arrays.stream(roomIds.split(","))
                .map(String::trim)
                .filter(roomId -> !roomId.isBlank())
                .map(UUID::fromString)
                .collect(Collectors.toUnmodifiableSet());
    }
}
