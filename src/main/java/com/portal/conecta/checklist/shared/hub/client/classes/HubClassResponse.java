package com.portal.conecta.checklist.shared.hub.client.classes;

import com.portal.conecta.checklist.module.checklist.domain.enums.Shift;

import java.time.Instant;
import java.util.UUID;

public record HubClassResponse(
        UUID id,
        Shift shift,
        Integer number,
        String name,
        UUID courseId,
        Instant createdAt
) {
}
