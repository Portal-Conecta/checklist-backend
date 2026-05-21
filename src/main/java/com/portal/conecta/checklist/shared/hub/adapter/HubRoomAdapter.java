package com.portal.conecta.checklist.shared.hub.adapter;

import com.portal.conecta.checklist.shared.hub.client.HubRoomClient;
import com.portal.conecta.checklist.shared.hub.dto.RoomDTO;
import com.portal.conecta.checklist.shared.hub.port.HubRoomPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("!mock")
@RequiredArgsConstructor
public class HubRoomAdapter implements HubRoomPort {

    private final HubRoomClient hubRoomClient;

    @Override
    public RoomDTO findById(UUID roomId) {
        return hubRoomClient.findById(roomId);
    }
}
