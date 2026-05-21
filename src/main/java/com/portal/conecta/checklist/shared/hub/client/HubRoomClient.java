package com.portal.conecta.checklist.shared.hub.client;

import com.portal.conecta.checklist.shared.hub.dto.RoomDTO;
import feign.Param;
import feign.RequestLine;

import java.util.UUID;

public interface HubRoomClient {

    @RequestLine("GET /api/rooms/{roomId}")
    RoomDTO findById(@Param("roomId") UUID roomId);
}
