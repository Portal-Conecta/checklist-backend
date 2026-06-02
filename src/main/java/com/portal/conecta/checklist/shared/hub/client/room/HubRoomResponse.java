package com.portal.conecta.checklist.shared.hub.client.room;

import com.portal.conecta.checklist.module.checklist.domain.valueobject.RoomReference;

import java.util.UUID;

public record HubRoomResponse(UUID id, Integer number, String typeRoom, String status) {

    public RoomReference toReference(UUID requestedRoomId) {
        UUID referenceId = id == null ? requestedRoomId : id;
        return new RoomReference(referenceId, number, typeRoom, status);
    }
}
