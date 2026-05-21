package com.portal.conecta.checklist.shared.hub.mock;

import com.portal.conecta.checklist.shared.hub.dto.RoomDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HubRoomMockTest {

    private final HubRoomMock hubRoomMock = new HubRoomMock();

    private static final UUID ROOM_101 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ROOM_102 = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ROOM_103 = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID UNKNOWN  = UUID.fromString("99999999-9999-9999-9999-999999999999");

    @Test
    @DisplayName("Deve retornar sala 101 do tipo WORKSHOP para UUID conhecido")
    void deveRetornarSala101() {
        RoomDTO room = hubRoomMock.findById(ROOM_101);

        assertNotNull(room);
        assertEquals(ROOM_101, room.id());
        assertEquals("WORKSHOP", room.type());
        assertEquals("101", room.number());
    }

    @Test
    @DisplayName("Deve retornar sala 102 do tipo CLASSROOM para UUID conhecido")
    void deveRetornarSala102() {
        RoomDTO room = hubRoomMock.findById(ROOM_102);

        assertNotNull(room);
        assertEquals(ROOM_102, room.id());
        assertEquals("CLASSROOM", room.type());
        assertEquals("102", room.number());
    }

    @Test
    @DisplayName("Deve retornar sala 103 do tipo LAB para UUID conhecido")
    void deveRetornarSala103() {
        RoomDTO room = hubRoomMock.findById(ROOM_103);

        assertNotNull(room);
        assertEquals(ROOM_103, room.id());
        assertEquals("LAB", room.type());
        assertEquals("103", room.number());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException para UUID desconhecido")
    void deveLancarExcecaoParaUUIDDesconhecido() {
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> hubRoomMock.findById(UNKNOWN)
        );

        assertTrue(exception.getMessage().contains(UNKNOWN.toString()));
    }
}
