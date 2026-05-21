package com.portal.conecta.checklist.shared.hub.dto;

import java.util.UUID;

public record RoomDTO(
        UUID id,
        String type,
        String number
) {}
