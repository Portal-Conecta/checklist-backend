package com.portal.conecta.checklist.module.checklist.presentation.dto.request;

import java.util.List;

public record ChecklistExecutionSubmitDTO(
        List<ChecklistAnswerRequestDTO> answers
) {}
