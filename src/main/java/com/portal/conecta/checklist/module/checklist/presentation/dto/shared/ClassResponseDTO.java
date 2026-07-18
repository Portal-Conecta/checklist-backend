package com.portal.conecta.checklist.module.checklist.presentation.dto.shared;

import com.portal.conecta.checklist.module.checklist.domain.enums.Shift;

import java.time.Instant;
import java.util.UUID;

public record ClassResponseDTO(
        UUID id,
        String name,
        Integer number,
        Shift shift,
        UUID courseId,
        Instant createdAt
) {
}
