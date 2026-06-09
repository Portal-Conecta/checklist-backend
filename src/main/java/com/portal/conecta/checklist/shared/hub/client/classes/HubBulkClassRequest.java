package com.portal.conecta.checklist.shared.hub.client.classes;

import java.util.List;
import java.util.UUID;

public record HubBulkClassRequest(List<UUID> ids) {
}
