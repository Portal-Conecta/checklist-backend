package com.portal.conecta.checklist.shared.hub.client.room;

import java.util.List;
import java.util.UUID;

public record HubBulkRoomResponse(
        List<HubRoomResponse> items,
        List<UUID> foundIds,
        List<UUID> missingIds
) {
}
