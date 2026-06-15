package com.portal.conecta.checklist.modules.checklist.presentation.mapper;

import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistSubmissionWindow;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.window.response.SubmissionWindowResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubmissionWindowMapper {

    public SubmissionWindowResponseDTO toResponse(ChecklistSubmissionWindow window) {
        return new SubmissionWindowResponseDTO(
                window.getId(),
                window.getClassId(),
                window.getShift(),
                window.getChecklistType(),
                window.getOpenAt(),
                window.getDurationMinutes(),
                window.getCreatedAt(),
                window.getUpdatedAt()
        );
    }

    public List<SubmissionWindowResponseDTO> toResponseList(List<ChecklistSubmissionWindow> windows) {
        return windows.stream().map(this::toResponse).toList();
    }
}
