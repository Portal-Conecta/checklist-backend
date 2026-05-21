package com.portal.conecta.checklist.shared.hub.mock;

import com.portal.conecta.checklist.shared.hub.dto.RoomDTO;
import com.portal.conecta.checklist.shared.hub.port.HubRoomPort;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@Profile("mock")
public class HubRoomMock implements HubRoomPort {

    private static final Map<UUID, RoomDTO> ROOMS = Map.of(
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            new RoomDTO(UUID.fromString("11111111-1111-1111-1111-111111111111"), "WORKSHOP", "101"),

            UUID.fromString("22222222-2222-2222-2222-222222222222"),
            new RoomDTO(UUID.fromString("22222222-2222-2222-2222-222222222222"), "CLASSROOM", "102"),

            UUID.fromString("33333333-3333-3333-3333-333333333333"),
            new RoomDTO(UUID.fromString("33333333-3333-3333-3333-333333333333"), "LAB", "103")
    );

    @Override
    public RoomDTO findById(UUID roomId) {
        RoomDTO room = ROOMS.get(roomId);
        if (room == null) {
            throw new EntityNotFoundException("Sala não encontrada (mock): " + roomId);
        }
        return room;
    }
}
