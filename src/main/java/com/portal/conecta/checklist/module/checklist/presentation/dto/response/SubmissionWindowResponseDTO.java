package com.portal.conecta.checklist.module.checklist.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.module.checklist.domain.enums.Shift;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

public record SubmissionWindowResponseDTO(
        UUID id,
        Shift shift,
        ChecklistType checklistType,
        @JsonFormat(pattern = "HH:mm") LocalTime openAt,
        int durationMinutes,
        Instant createdAt,
        Instant updatedAt
) {}
