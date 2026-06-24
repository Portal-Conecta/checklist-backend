package com.portal.conecta.checklist.modules.checklist.presentation.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.issues.presentation.mapper.ChecklistIssueMapper;
import com.portal.conecta.checklist.modules.checklist.domain.valueobject.ClassReference;
import com.portal.conecta.checklist.modules.checklist.domain.valueobject.RoomReference;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.shared.ClassResponseDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.shared.RoomResponseDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.execution.response.ChecklistAnswersDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.execution.response.ChecklistExecutionHistoryDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.execution.response.ChecklistExecutionResponseDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.execution.response.ChecklistExecutionSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ChecklistExecutionMapper {

    private final ObjectMapper objectMapper;
    private final ChecklistIssueMapper issueMapper;

    public ChecklistExecutionMapper(ObjectMapper objectMapper, ChecklistIssueMapper issueMapper) {
        this.objectMapper = objectMapper;
        this.issueMapper = issueMapper;
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

    public ChecklistAnswersDTO toAnswersDTO(Map<String, Object> answersJson) {
        if (answersJson == null || answersJson.isEmpty()) {
            return emptyAnswers();
        }

        return objectMapper.convertValue(answersJson, ChecklistAnswersDTO.class);
    }

    public ChecklistExecutionHistoryDTO toHistoryResponse(ChecklistExecution execution) {
        return toHistoryResponse(execution, null, null);
    }

    public ChecklistExecutionHistoryDTO toHistoryResponse(ChecklistExecution execution, RoomReference room, ClassReference classRef) {
        if (execution == null) {
            return null;
        }

        ChecklistTemplate template = execution.getChecklistTemplate();
        ChecklistAnswersDTO answers = toAnswersDTO(execution.getAnswersJson());

        RoomResponseDTO roomDTO = null;
        if (room != null) {
            roomDTO = new RoomResponseDTO(
                    room.getRoomId(),
                    room.getNumber(),
                    room.getTypeRoom(),
                    room.getStatus()
            );
        }

        ClassResponseDTO classDTO = null;
        if (classRef != null) {
            classDTO = new ClassResponseDTO(
                    classRef.getClassId(),
                    classRef.getName(),
                    classRef.getNumber(),
                    classRef.getShift(),
                    classRef.getCourseReference() != null ? classRef.getCourseReference().getCourseId() : null,
                    classRef.getCreatedAt()
            );
        }

        return new ChecklistExecutionHistoryDTO(
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
                toInstant(execution.getStartedAt()),
                toInstant(execution.getSubmittedAt()),
                answers.summary(),
                roomDTO,
                classDTO
        );
    }

    public Page<ChecklistExecutionHistoryDTO> toPageHistory(Page<ChecklistExecution> executions) {
        return toPageHistory(executions, Map.of(), Map.of());
    }

    public Page<ChecklistExecutionHistoryDTO> toPageHistory(Page<ChecklistExecution> executions, Map<UUID, RoomReference> roomMap, Map<UUID, ClassReference> classMap) {
        if (executions == null) {
            return Page.empty();
        }
        return executions.map(execution -> toHistoryResponse(
                execution,
                roomMap != null ? roomMap.get(execution.getRoomId()) : null,
                classMap != null ? classMap.get(execution.getClassId()) : null
        ));
    }

    public List<ChecklistExecutionHistoryDTO> toHistoryResponseList(List<ChecklistExecution> executions) {
        return toHistoryResponseList(executions, Map.of(), Map.of());
    }

    public List<ChecklistExecutionHistoryDTO> toHistoryResponseList(List<ChecklistExecution> executions, Map<UUID, RoomReference> roomMap, Map<UUID, ClassReference> classMap) {
        if (executions == null) {
            return List.of();
        }
        return executions.stream()
                .map(execution -> toHistoryResponse(
                        execution,
                        roomMap != null ? roomMap.get(execution.getRoomId()) : null,
                        classMap != null ? classMap.get(execution.getClassId()) : null
                ))
                .toList();
    }

    private ChecklistAnswersDTO emptyAnswers() {
        return new ChecklistAnswersDTO(
                List.of(),
                new ChecklistExecutionSummaryDTO(0, 0, 0, 0)
        );
    }

    private Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null
                ? null
                : dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
}
