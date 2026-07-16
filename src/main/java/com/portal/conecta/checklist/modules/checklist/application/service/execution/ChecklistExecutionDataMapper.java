package com.portal.conecta.checklist.modules.checklist.application.service.execution;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.update.UpdateChecklistAnswerCommand;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.create.CreateChecklistExecutionCommand;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.submit.SubmitChecklistExecutionCommand;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ConformityAnswerValue;
import com.portal.conecta.checklist.modules.checklist.domain.enums.Period;
import com.portal.conecta.checklist.modules.checklist.domain.enums.Shift;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChecklistExecutionDataMapper {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public ChecklistExecution toDraftEntity(
            CreateChecklistExecutionCommand command,
            ChecklistTemplate template,
            UUID filledBy,
            LocalDateTime startedAt,
            Shift shift,
            Period period
    ) {
        return ChecklistExecution.builder()
                .checklistTemplate(template)
                .roomId(template.getRoomId())
                .classId(command.classId())
                .userId(filledBy)
                .shift(shift)
                .period(period)
                .checklistType(command.checklistType())
                .category(template.getCategory())
                .status(ChecklistExecutionStatus.DRAFT)
                .answersJson(emptyAnswersJson())
                .startedAt(startedAt)
                .build();
    }

    public Map<String, Object> toAnswersJson(SubmitChecklistExecutionCommand command) {
        List<AnswerData> answers = command.answers().stream()
                .map(this::toAnswerData)
                .toList();

        return objectMapper.convertValue(
                new AnswersData(answers, summarize(answers)),
                MAP_TYPE
        );
    }

    private Map<String, Object> emptyAnswersJson() {
        return objectMapper.convertValue(
                new AnswersData(List.of(), new SummaryData(0, 0, 0, 0)),
                MAP_TYPE
        );
    }

    private AnswerData toAnswerData(UpdateChecklistAnswerCommand answer) {
        Boolean compliant = answer.value() == null
                ? null
                : answer.value() == ConformityAnswerValue.COMPLIANT;

        return new AnswerData(
                answer.itemKey(),
                answer.value(),
                compliant,
                answer.observation(),
                answer.answeredAt()
        );
    }

    private SummaryData summarize(List<AnswerData> answers) {
        int answered = (int) answers.stream().filter(answer -> answer.value() != null).count();
        int compliant = (int) answers.stream().filter(answer -> Boolean.TRUE.equals(answer.compliant())).count();
        int nonCompliant = (int) answers.stream().filter(answer -> Boolean.FALSE.equals(answer.compliant())).count();
        return new SummaryData(answers.size(), answered, compliant, nonCompliant);
    }

    private record AnswersData(List<AnswerData> answers, SummaryData summary) {
    }

    private record AnswerData(
            String itemKey,
            ConformityAnswerValue value,
            Boolean compliant,
            String observation,
            java.time.Instant answeredAt
    ) {
    }

    private record SummaryData(
            Integer totalItems,
            Integer answeredItems,
            Integer compliantItems,
            Integer nonCompliantItems
    ) {
    }
}
