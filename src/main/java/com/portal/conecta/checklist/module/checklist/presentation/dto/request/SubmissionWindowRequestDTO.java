package com.portal.conecta.checklist.module.checklist.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record SubmissionWindowRequestDTO(

        @NotNull(message = "openAt e obrigatorio.")
        @JsonFormat(pattern = "HH:mm")
        LocalTime openAt,

        @NotNull(message = "durationMinutes e obrigatorio.")
        @Min(value = 1, message = "durationMinutes deve ser no minimo 1.")
        @Max(value = 1439, message = "durationMinutes nao pode ultrapassar 1439 (meia-noite).")
        Integer durationMinutes
) {}
