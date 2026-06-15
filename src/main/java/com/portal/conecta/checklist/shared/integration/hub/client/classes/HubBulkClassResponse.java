package com.portal.conecta.checklist.shared.integration.hub.client.classes;

import java.util.List;
import java.util.UUID;

public record HubBulkClassResponse(
        List<HubClassResponse> items,
        List<UUID> foundIds,
        List<UUID> missingIds
) {
}
