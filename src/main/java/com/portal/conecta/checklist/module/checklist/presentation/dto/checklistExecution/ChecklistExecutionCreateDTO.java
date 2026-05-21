package com.portal.conecta.checklist.module.checklist.presentation.dto.checklistExecution;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.domain.model.Status;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.ClassReference;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.RoomReference;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.UserReference;

import java.util.Map;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record ChecklistExecutionCreateDTO(

        @NotNull UUID roomId,
        @NotNull UUID classId,
        @NotNull UUID userId,
        @NotNull UUID templateId,
        Map<String, Object> answersJson
) {}
