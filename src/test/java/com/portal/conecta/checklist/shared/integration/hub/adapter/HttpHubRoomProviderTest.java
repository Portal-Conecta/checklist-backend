package com.portal.conecta.checklist.shared.integration.hub.adapter;

import com.portal.conecta.checklist.shared.integration.hub.client.room.HubRoomClient;
import com.portal.conecta.checklist.shared.integration.hub.client.room.HubRoomResponse;
import com.portal.conecta.checklist.shared.integration.hub.adapter.room.HttpHubRoomProvider;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpHubRoomProviderTest {

    private final HubRoomClient hubRoomClient = mock(HubRoomClient.class);
    private final HttpHubRoomProvider provider = new HttpHubRoomProvider(hubRoomClient);

    @Test
    void shouldTranslateHubRoomResponseToRoomReference() {
        UUID roomId = UUID.randomUUID();

        when(hubRoomClient.findById(roomId)).thenReturn(new HubRoomResponse(
                roomId,
                101,
                "LABORATORY",
                "ACTIVE"
        ));

        var reference = provider.findById(roomId);

        assertTrue(reference.isPresent());
        assertEquals(roomId, reference.orElseThrow().getRoomId());
        assertEquals(101, reference.orElseThrow().getNumber());
        assertEquals("LABORATORY", reference.orElseThrow().getTypeRoom());
        assertEquals("ACTIVE", reference.orElseThrow().getStatus());
    }
}
