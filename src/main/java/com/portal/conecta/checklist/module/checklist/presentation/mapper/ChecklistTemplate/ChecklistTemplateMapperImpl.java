package com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistTemplate;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.domain.model.Status;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.RoomReference;
import com.portal.conecta.checklist.module.checklist.presentation.dto.checklistTemplate.ChecklistTemplateCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.checklistTemplate.ChecklistTemplateResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ChecklistTemplateMapperImpl implements ChecklistTemplateMapper{

    @Override
    public ChecklistTemplate toEntity (ChecklistTemplateCreateDTO req){
        if (req == null){
            return null;
        }
        return ChecklistTemplate.builder()
                .roomId(req.roomId())
                .title(req.title())
                .description(req.description())
                .schemaJson(req.schemaJson())
                .version(1)
                .status(Status.DRAFT)
                .active(true)
                .build();
    }
    @Override
    public ChecklistTemplateResponseDTO toResponse(ChecklistTemplate entity, RoomReference roomReference){
        if(entity == null) {
            return null;
        }
        return new ChecklistTemplateResponseDTO(
                entity.getId(),
                roomReference,
                entity.getTitle(),
                entity.getDescription(),
                entity.getVersion(),
                entity.getStatus(),
                entity.isActive(),
                entity.getSchemaJson()
        );
    }

}
