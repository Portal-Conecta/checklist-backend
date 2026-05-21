package com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistTemplate;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.RoomReference;
import com.portal.conecta.checklist.module.checklist.presentation.dto.checklistTemplate.ChecklistTemplateCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.checklistTemplate.ChecklistTemplateResponseDTO;

public interface ChecklistTemplateMapper {

    ChecklistTemplate toEntity(ChecklistTemplateCreateDTO req);
    ChecklistTemplateResponseDTO toResponse(ChecklistTemplate entity, RoomReference roomReference);
}
