package com.portal.conecta.checklist.shared.integration.hub.client.me;

import java.util.List;
import java.util.UUID;

public record HubMyCourseResponse(
        UUID id,
        String name,
        String code,
        List<HubMyClassResponse> classes
) {
}
