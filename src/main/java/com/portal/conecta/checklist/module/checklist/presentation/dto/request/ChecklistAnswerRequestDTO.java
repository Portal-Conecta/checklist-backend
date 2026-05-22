package com.portal.conecta.checklist.module.checklist.presentation.dto.request;

import com.portal.conecta.checklist.module.checklist.domain.enums.ConformityAnswerValue;

import java.time.Instant;

public record ChecklistAnswerRequestDTO(
        String itemKey,
        ConformityAnswerValue value,
        String observation,
        Instant answeredAt
) {}
