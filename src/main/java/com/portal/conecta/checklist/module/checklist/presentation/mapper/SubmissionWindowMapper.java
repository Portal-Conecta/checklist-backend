package com.portal.conecta.checklist.module.checklist.presentation.mapper;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistSubmissionWindow;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.SubmissionWindowResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubmissionWindowMapper {

    public SubmissionWindowResponseDTO toResponse(ChecklistSubmissionWindow window) {
        return new SubmissionWindowResponseDTO(
                window.getId(),
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
