package com.portal.conecta.checklist.shared.integration.hub.client.room;

import java.util.UUID;

public record HubRoomResponse(UUID id, Integer number, String typeRoom, String status) {
}
