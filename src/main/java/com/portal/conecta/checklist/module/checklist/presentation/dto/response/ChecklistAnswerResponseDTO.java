package com.portal.conecta.checklist.module.checklist.presentation.dto.response;

import com.portal.conecta.checklist.module.checklist.domain.enums.ConformityAnswerValue;

import java.time.Instant;

public record ChecklistAnswerResponseDTO(
        String itemKey,
        ConformityAnswerValue value,
        Boolean compliant,
        String observation,
        Instant answeredAt
) {}
