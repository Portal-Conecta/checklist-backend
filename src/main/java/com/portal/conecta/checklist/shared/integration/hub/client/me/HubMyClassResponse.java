package com.portal.conecta.checklist.shared.integration.hub.client.me;

import com.portal.conecta.checklist.module.checklist.domain.enums.Shift;

import java.util.UUID;

public record HubMyClassResponse(
        UUID id,
        String name,
        Integer number,
        Shift shift,
        String classRole
) {
}
