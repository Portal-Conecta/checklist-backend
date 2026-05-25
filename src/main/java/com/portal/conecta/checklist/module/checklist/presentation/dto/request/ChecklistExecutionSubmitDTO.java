package com.portal.conecta.checklist.module.checklist.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ChecklistExecutionSubmitDTO(
        @Valid
        @NotEmpty(message = "answers nao pode estar vazio.")
        List<ChecklistAnswerRequestDTO> answers
) {}
