package com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecution;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.ClassReference;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.RoomReference;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.UserReference;
import com.portal.conecta.checklist.module.checklist.presentation.dto.checklistExecution.ChecklistExecutionCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.checklistExecution.ChecklistExecutionResponseDTO;

public interface ChecklistExecutionMapper {

    ChecklistExecution toEntity(ChecklistExecutionCreateDTO dto, ChecklistTemplate template);

    ChecklistExecutionResponseDTO toResponse(
            ChecklistExecution entity,
            RoomReference room,
            ClassReference classe,
            UserReference user
    );
}
