package com.portal.conecta.checklist.unit.checklist.presentation.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.modules.checklist.domain.enums.Period;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.issues.presentation.mapper.ChecklistIssueMapper;
import com.portal.conecta.checklist.modules.checklist.presentation.mapper.ChecklistExecutionMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ChecklistExecutionMapperTest {

    private final ChecklistExecutionMapper mapper = new ChecklistExecutionMapper(
            new ObjectMapper(),
            new ChecklistIssueMapper()
    );

    @Test
    void shouldMapExecutionToResponse() {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ChecklistExecution execution = ChecklistExecution.builder()
                .checklistTemplate(ChecklistTemplate.builder().id(templateId).version(2).build())
                .roomId(roomId)
                .classId(classId)
                .userId(userId)
                .period(Period.MORNING)
                .checklistType(ChecklistType.ARRIVAL)
                .status(ChecklistExecutionStatus.DRAFT)
                .answersJson(Map.of(
                        "answers", List.of(),
                        "summary", Map.of(
                                "totalItems", 0,
                                "answeredItems", 0,
                                "compliantItems", 0,
                                "nonCompliantItems", 0
                        )
                ))
                .build();

        var response = mapper.toResponse(execution);

        assertThat(response.templateId()).isEqualTo(templateId);
        assertThat(response.templateVersion()).isEqualTo(2);
        assertThat(response.roomId()).isEqualTo(roomId);
        assertThat(response.classId()).isEqualTo(classId);
        assertThat(response.filledBy()).isEqualTo(userId);
        assertThat(response.summary().totalItems()).isZero();
    }
}
