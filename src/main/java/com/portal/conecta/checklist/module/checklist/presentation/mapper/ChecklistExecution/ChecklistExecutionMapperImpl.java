package com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecution;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.domain.model.Status;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.ClassReference;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.RoomReference;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.UserReference;
import com.portal.conecta.checklist.module.checklist.presentation.dto.checklistExecution.ChecklistExecutionCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.checklistExecution.ChecklistExecutionResponseDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;

@Component
public class ChecklistExecutionMapperImpl implements ChecklistExecutionMapper {

    @Override
    public ChecklistExecution toEntity(ChecklistExecutionCreateDTO dto, ChecklistTemplate template) {
        if (dto == null) {
            return null;
        }

        return ChecklistExecution.builder()
                .checklistTemplate(template)
                .roomId(dto.roomId())
                .classId(dto.classId())
                .userId(dto.userId())
                .status(Status.DRAFT)
                .answersJson(dto.answersJson() != null ? dto.answersJson() : java.util.Collections.emptyMap())
                .complianceScore(BigDecimal.ZERO)
                .issues(new ArrayList<>())
                .build();
    }

    @Override
    public ChecklistExecutionResponseDTO toResponse(
            ChecklistExecution entity,
            RoomReference room,
            ClassReference clazz,
            UserReference user) {

        if (entity == null) {
            return null;
        }

        return new ChecklistExecutionResponseDTO(
                entity.getId(),
                entity.getChecklistTemplate().getId(),
                room,
                clazz,
                user,
                entity.getStatus(),
                entity.getAnswersJson(),
                entity.getComplianceScore()
        );
    }
}