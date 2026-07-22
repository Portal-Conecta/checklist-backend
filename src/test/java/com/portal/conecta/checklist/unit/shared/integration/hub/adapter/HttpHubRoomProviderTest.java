package com.portal.conecta.checklist.unit.shared.integration.hub.adapter;

import com.portal.conecta.checklist.module.checklist.domain.valueobject.RoomReference;
import com.portal.conecta.checklist.shared.integration.hub.adapter.room.HttpHubRoomProvider;
import com.portal.conecta.checklist.shared.integration.hub.client.room.HubBulkRoomRequest;
import com.portal.conecta.checklist.shared.integration.hub.client.room.HubBulkRoomResponse;
import com.portal.conecta.checklist.shared.integration.hub.client.room.HubRoomClient;
import com.portal.conecta.checklist.shared.integration.hub.client.room.HubRoomResponse;
import com.portal.conecta.checklist.shared.integration.hub.exception.HubIntegrationException;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Test
    @DisplayName("findByIds deve retornar lista de RoomReference para IDs encontrados")
    void findByIds_shouldReturnRoomReferences() {
        UUID roomId1 = UUID.randomUUID();
        UUID roomId2 = UUID.randomUUID();
        List<HubRoomResponse> items = List.of(
                new HubRoomResponse(roomId1, 101, "LABORATORY", "active"),
                new HubRoomResponse(roomId2, 202, "CLASSROOM", "active")
        );
        HubBulkRoomResponse bulkResponse = new HubBulkRoomResponse(items, List.of(roomId1, roomId2), List.of());

        when(hubRoomClient.findBulk(any(HubBulkRoomRequest.class))).thenReturn(bulkResponse);

        List<RoomReference> result = provider.findByIds(List.of(roomId1, roomId2));

        assertEquals(2, result.size());
        assertEquals(roomId1, result.get(0).getRoomId());
        assertEquals(101, result.get(0).getNumber());
        assertEquals(roomId2, result.get(1).getRoomId());
        assertEquals(202, result.get(1).getNumber());
    }

    @Test
    @DisplayName("findByIds com lista vazia deve retornar lista vazia sem chamar Hub")
    void findByIds_withEmptyList_shouldReturnEmpty() {
        List<RoomReference> result = provider.findByIds(List.of());

        assertTrue(result.isEmpty());
        verifyNoInteractions(hubRoomClient);
    }

    @Test
    @DisplayName("findByIds deve deduplicar IDs antes de chamar Hub")
    void findByIds_shouldDeduplicateIds() {
        UUID roomId = UUID.randomUUID();
        HubBulkRoomResponse bulkResponse = new HubBulkRoomResponse(
                List.of(new HubRoomResponse(roomId, 101, "LABORATORY", "active")),
                List.of(roomId),
                List.of()
        );

        when(hubRoomClient.findBulk(any(HubBulkRoomRequest.class))).thenReturn(bulkResponse);

        List<RoomReference> result = provider.findByIds(List.of(roomId, roomId, roomId));

        assertEquals(1, result.size());
        // O Hub deve ter sido chamado apenas uma vez com IDs únicos
        verify(hubRoomClient, times(1)).findBulk(any(HubBulkRoomRequest.class));
    }

    @Test
    @DisplayName("findByIds deve lancar HubIntegrationException em falha de comunicacao")
    void findByIds_shouldThrowHubIntegrationExceptionOnFeignError() {
        UUID roomId = UUID.randomUUID();

        when(hubRoomClient.findBulk(any(HubBulkRoomRequest.class)))
                .thenThrow(mock(FeignException.class));

        assertThrows(HubIntegrationException.class, () -> provider.findByIds(List.of(roomId)));
    }
}
