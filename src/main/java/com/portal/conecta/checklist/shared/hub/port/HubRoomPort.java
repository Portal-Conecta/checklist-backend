package com.portal.conecta.checklist.shared.hub.port;

import com.portal.conecta.checklist.shared.hub.dto.RoomDTO;

import java.util.UUID;

public interface HubRoomPort {

    RoomDTO findById(UUID roomId);
}
