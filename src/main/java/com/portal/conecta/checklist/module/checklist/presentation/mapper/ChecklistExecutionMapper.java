package com.portal.conecta.checklist.module.checklist.presentation.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.enums.ConformityAnswerValue;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistAnswerRequestDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistAnswerResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistAnswersDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionSummaryDTO;
import com.portal.conecta.checklist.module.issues.presentation.mapper.ChecklistIssueMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
public class ChecklistExecutionMapper {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final ChecklistIssueMapper issueMapper;

    public ChecklistExecutionMapper(ObjectMapper objectMapper, ChecklistIssueMapper issueMapper) {
        this.objectMapper = objectMapper;
        this.issueMapper = issueMapper;
    }

    public ChecklistExecution toDraftEntity(
            ChecklistExecutionDraftCreateDTO request,
            ChecklistTemplate template,
            UUID filledBy,
            LocalDateTime startedAt
    ) {
        if (request == null) {
            return null;
        }

        return ChecklistExecution.builder()
                .checklistTemplate(template)
                .roomId(request.roomId())
                .classId(request.classId())
                .userId(filledBy)
                .period(request.period())
                .checklistType(request.checklistType())
                .status(ChecklistExecutionStatus.DRAFT)
                .answersJson(toAnswersJson(emptyAnswers()))
                .startedAt(startedAt)
                .build();
    }

    public ChecklistExecutionResponseDTO toResponse(ChecklistExecution execution) {
        if (execution == null) {
            return null;
        }

        ChecklistTemplate template = execution.getChecklistTemplate();
        ChecklistAnswersDTO answers = toAnswersDTO(execution.getAnswersJson());

        return new ChecklistExecutionResponseDTO(
                execution.getId(),
                template == null ? null : template.getId(),
                template == null ? null : template.getVersion(),
                execution.getRoomId(),
                execution.getClassId(),
                execution.getUserId(),
                execution.getPeriod(),
                execution.getChecklistType(),
                execution.getStatus(),
                execution.getComplianceScore(),
                answers,
                answers.summary(),
                toInstant(execution.getStartedAt()),
                toInstant(execution.getSubmittedAt()),
                issueMapper.toResponseList(execution.getIssues())
        );
    }

    public Map<String, Object> toAnswersJson(ChecklistExecutionSubmitDTO request) {
        if (request == null) {
            return toAnswersJson(emptyAnswers());
        }

        List<ChecklistAnswerResponseDTO> answers = safeAnswers(request.answers()).stream()
                .map(this::toAnswerResponse)
                .filter(Objects::nonNull)
                .toList();

        return toAnswersJson(new ChecklistAnswersDTO(answers, summarize(answers)));
    }

    public Map<String, Object> toAnswersJson(ChecklistAnswersDTO answers) {
        return objectMapper.convertValue(answers == null ? emptyAnswers() : answers, MAP_TYPE);
    }

    public ChecklistAnswersDTO toAnswersDTO(Map<String, Object> answersJson) {
        if (answersJson == null || answersJson.isEmpty()) {
            return emptyAnswers();
        }

        return objectMapper.convertValue(answersJson, ChecklistAnswersDTO.class);
    }

    public ChecklistAnswerResponseDTO toAnswerResponse(ChecklistAnswerRequestDTO answer) {
        if (answer == null) {
            return null;
        }

        Boolean compliant = answer.value() == null ? null : answer.value() == ConformityAnswerValue.COMPLIANT;

        return new ChecklistAnswerResponseDTO(
                answer.itemKey(),
                answer.value(),
                compliant,
                answer.observation(),
                answer.answeredAt()
        );
    }

    private ChecklistAnswersDTO emptyAnswers() {
        return new ChecklistAnswersDTO(List.of(), new ChecklistExecutionSummaryDTO(0, 0, 0, 0));
    }

    private ChecklistExecutionSummaryDTO summarize(List<ChecklistAnswerResponseDTO> answers) {
        int totalItems = answers.size();
        int answeredItems = (int) answers.stream()
                .filter(answer -> answer.value() != null)
                .count();
        int compliantItems = (int) answers.stream()
                .filter(answer -> Boolean.TRUE.equals(answer.compliant()))
                .count();
        int nonCompliantItems = (int) answers.stream()
                .filter(answer -> Boolean.FALSE.equals(answer.compliant()))
                .count();

        return new ChecklistExecutionSummaryDTO(totalItems, answeredItems, compliantItems, nonCompliantItems);
    }

    private List<ChecklistAnswerRequestDTO> safeAnswers(List<ChecklistAnswerRequestDTO> answers) {
        return answers == null ? List.of() : answers;
    }

    private Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
}
