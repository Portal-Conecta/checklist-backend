package com.portal.conecta.checklist.unit.checklist.presentation.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.application.port.out.integration.HubClassProvider;
import com.portal.conecta.checklist.module.checklist.application.port.out.integration.HubRoomProvider;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.module.checklist.domain.enums.Period;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.presentation.port.ExecutionIssuesQueryPort;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ChecklistExecutionMapperTest {

    private final ExecutionIssuesQueryPort issuesQueryPort = Mockito.mock(ExecutionIssuesQueryPort.class);

    private final ChecklistExecutionMapper mapper = new ChecklistExecutionMapper(
            new ObjectMapper(),
            issuesQueryPort,
            Mockito.mock(HubRoomProvider.class),
            Mockito.mock(HubClassProvider.class)
    );

    {
        when(issuesQueryPort.findByExecutionId(any())).thenReturn(List.of());
    }

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
        assertThat(response.submittedBy()).isNull();
        assertThat(response.canceledBy()).isNull();
        assertThat(response.summary().totalItems()).isZero();
    }

    @Test
    void shouldMapSubmittedByAndCanceledByWhenPresent() {
        UUID submitterId = UUID.randomUUID();
        UUID cancelerId = UUID.randomUUID();

        ChecklistExecution execution = ChecklistExecution.builder()
                .checklistTemplate(ChecklistTemplate.builder().id(UUID.randomUUID()).version(1).build())
                .roomId(UUID.randomUUID())
                .classId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .submittedBy(submitterId)
                .canceledBy(cancelerId)
                .period(Period.MORNING)
                .checklistType(ChecklistType.ARRIVAL)
                .status(ChecklistExecutionStatus.CANCELED)
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
        var history = mapper.toHistoryResponse(execution);

        assertThat(response.submittedBy()).isEqualTo(submitterId);
        assertThat(response.canceledBy()).isEqualTo(cancelerId);
        assertThat(history.submittedBy()).isEqualTo(submitterId);
        assertThat(history.canceledBy()).isEqualTo(cancelerId);
    }
}
