package com.portal.conecta.checklist.shared.hub.client.room;

import java.util.List;
import java.util.UUID;

public record HubBulkRoomRequest(List<UUID> ids) {
}
