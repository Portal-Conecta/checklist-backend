package com.portal.conecta.checklist.module.checklist.presentation.dto.response;

import java.util.List;

public record ChecklistAnswersDTO(
        List<ChecklistAnswerResponseDTO> answers,
        ChecklistExecutionSummaryDTO summary
) {}
