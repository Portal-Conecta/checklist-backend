package com.portal.conecta.checklist.shared.hub.adapter;

import com.portal.conecta.checklist.shared.hub.client.HubRoomClient;
import com.portal.conecta.checklist.shared.hub.dto.RoomDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HubRoomAdapterTest {

    private final HubRoomClient hubRoomClient = mock(HubRoomClient.class);
    private final HubRoomAdapter adapter = new HubRoomAdapter(hubRoomClient);

    @Test
    @DisplayName("Deve retornar RoomDTO ao delegar para o cliente Feign")
    void deveRetornarRoomDTOAoDelegarParaCliente() {
        UUID roomId = UUID.randomUUID();
        RoomDTO expected = new RoomDTO(roomId, "WORKSHOP", "101");

        when(hubRoomClient.findById(roomId)).thenReturn(expected);

        RoomDTO result = adapter.findById(roomId);

        assertNotNull(result);
        assertEquals(expected, result);
        verify(hubRoomClient, times(1)).findById(roomId);
    }

    @Test
    @DisplayName("Deve propagar EntityNotFoundException quando o cliente lançar")
    void devePropagarEntityNotFoundExceptionDoCliente() {
        UUID roomId = UUID.randomUUID();

        when(hubRoomClient.findById(roomId))
                .thenThrow(new EntityNotFoundException("Sala não encontrada no Hub"));

        assertThrows(EntityNotFoundException.class, () -> adapter.findById(roomId));
        verify(hubRoomClient, times(1)).findById(roomId);
    }
}
