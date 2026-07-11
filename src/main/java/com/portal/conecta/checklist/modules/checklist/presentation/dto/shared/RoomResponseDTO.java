package com.portal.conecta.checklist.modules.checklist.presentation.dto.shared;

import java.util.UUID;

public record RoomResponseDTO(
        UUID id,
        Integer number,
        String typeRoom,
        String status
) {
}
