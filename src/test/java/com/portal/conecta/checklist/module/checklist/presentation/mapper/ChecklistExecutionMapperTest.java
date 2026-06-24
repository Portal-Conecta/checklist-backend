package com.portal.conecta.checklist.module.checklist.presentation.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.module.checklist.domain.enums.Period;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistAnswersDTO;
import com.portal.conecta.checklist.module.issues.presentation.mapper.ChecklistIssueMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ChecklistExecutionMapperTest {

    private final ChecklistExecutionMapper mapper = new ChecklistExecutionMapper(
            new ObjectMapper(),
            new ChecklistIssueMapper()
    );

    @Test
    @DisplayName("deve mapear request de draft para entidade inicial")
    void deveMapearRequestDeDraftParaEntidadeInicial() {
        UUID templateId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        UUID filledBy = UUID.randomUUID();
        LocalDateTime startedAt = LocalDateTime.of(2026, 5, 22, 8, 0);
        ChecklistTemplate template = ChecklistTemplate.builder()
                .id(templateId)
                .version(1)
                .build();
        ChecklistExecutionDraftCreateDTO request = new ChecklistExecutionDraftCreateDTO(
                templateId,
                roomId,
                classId,
                Period.MORNING,
                ChecklistType.ARRIVAL
        );

        ChecklistExecution execution = mapper.toDraftEntity(request, template, filledBy, startedAt, null);
        ChecklistAnswersDTO answers = mapper.toAnswersDTO(execution.getAnswersJson());

        assertSame(template, execution.getChecklistTemplate());
        assertEquals(roomId, execution.getRoomId());
        assertEquals(classId, execution.getClassId());
        assertEquals(filledBy, execution.getUserId());
        assertEquals(Period.MORNING, execution.getPeriod());
        assertEquals(ChecklistType.ARRIVAL, execution.getChecklistType());
        assertEquals(ChecklistExecutionStatus.DRAFT, execution.getStatus());
        assertEquals(startedAt, execution.getStartedAt());
        assertNotNull(execution.getAnswersJson());
        assertEquals(0, answers.summary().totalItems());
        assertEquals(0, answers.summary().answeredItems());
    }
}
