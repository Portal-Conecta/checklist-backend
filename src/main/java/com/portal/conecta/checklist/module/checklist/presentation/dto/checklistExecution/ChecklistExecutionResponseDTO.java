package com.portal.conecta.checklist.module.checklist.presentation.dto.checklistExecution;

import com.portal.conecta.checklist.module.checklist.domain.model.Status;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.ClassReference;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.RoomReference;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.UserReference;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record ChecklistExecutionResponseDTO (
        UUID id,
        UUID templateId,
        RoomReference room,
        ClassReference clazz,
        UserReference user,
        Status status,
        Map<String, Object> answersJson,
        BigDecimal complianceScore

) {
}
