package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.enums.ConformityAnswerValue;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.UserReference;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistAnswerRequestDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistItemDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import com.portal.conecta.checklist.module.issues.domain.enums.IssuePriority;
import com.portal.conecta.checklist.module.issues.domain.enums.IssueStatus;
import com.portal.conecta.checklist.module.issues.domain.model.ChecklistIssue;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmitChecklistExecutionUseCase {

    private static final int ISSUE_DUE_DAYS = 7;

    private final ChecklistExecutionRepository executionRepository;
    private final ChecklistExecutionMapper executionMapper;
    private final ObjectMapper objectMapper;
    private final RequestContextProvider contextProvider;

    @Transactional
    public ChecklistExecution execute(UUID executionId, ChecklistExecutionSubmitDTO request) {
        ChecklistExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execucao de checklist nao encontrada."));

        var currentUser = contextProvider.getRequestContext();

        if (!execution.getUserId().equals(currentUser.userId())) {
            throw new AccessDeniedException("Usuario nao tem permissao para enviar esta execucao de checklist.");
        }

        if (execution.getStatus() != ChecklistExecutionStatus.DRAFT) {
            throw new IllegalStateException("Somente checklists em rascunho podem ser enviados.");
        }

        ChecklistSchemaDTO schema = objectMapper.convertValue(
                execution.getChecklistTemplate().getSchemaJson(),
                ChecklistSchemaDTO.class
        );

        Map<String, ChecklistItemDTO> itemsByKey = itemsByKey(schema);
        Map<String, ChecklistAnswerRequestDTO> answersByItemKey = answersByItemKey(request.answers());

        validateAnswers(itemsByKey, answersByItemKey);

        execution.setAnswersJson(executionMapper.toAnswersJson(request));
        execution.setComplianceScore(calculateComplianceScore(request.answers()));
        execution.setStatus(ChecklistExecutionStatus.SUBMITTED);
        execution.setSubmittedAt(LocalDateTime.now());
        createIssuesForNonCompliantAnswers(execution, request.answers(), itemsByKey);

        return executionRepository.save(execution);
    }

    private Map<String, ChecklistItemDTO> itemsByKey(ChecklistSchemaDTO schema) {
        return schema.sections().stream()
                .flatMap(section -> section.items().stream())
                .collect(Collectors.toMap(
                        ChecklistItemDTO::key,
                        Function.identity(),
                        (first, duplicated) -> {
                            throw new IllegalArgumentException("item.key duplicado no template: " + first.key());
                        },
                        LinkedHashMap::new
                ));
    }

    private Map<String, ChecklistAnswerRequestDTO> answersByItemKey(List<ChecklistAnswerRequestDTO> answers) {
        return answers.stream()
                .collect(Collectors.toMap(
                        ChecklistAnswerRequestDTO::itemKey,
                        Function.identity(),
                        (first, duplicated) -> {
                            throw new IllegalArgumentException("Resposta duplicada para itemKey: " + first.itemKey());
                        },
                        LinkedHashMap::new
                ));
    }

    private void validateAnswers(
            Map<String, ChecklistItemDTO> itemsByKey,
            Map<String, ChecklistAnswerRequestDTO> answersByItemKey
    ) {
        for (String answerItemKey : answersByItemKey.keySet()) {
            if (!itemsByKey.containsKey(answerItemKey)) {
                throw new IllegalArgumentException("Resposta enviada para item inexistente no template: " + answerItemKey);
            }
        }

        for (ChecklistItemDTO item : itemsByKey.values()) {
            ChecklistAnswerRequestDTO answer = answersByItemKey.get(item.key());

            if (Boolean.TRUE.equals(item.required()) && answer == null) {
                throw new IllegalArgumentException("Item obrigatorio sem resposta: " + item.key());
            }
        }

        answersByItemKey.values().forEach(answer -> {
            if (answer.value() == ConformityAnswerValue.NON_COMPLIANT && isBlank(answer.observation())) {
                throw new IllegalArgumentException("Item nao conforme exige observacao: " + answer.itemKey());
            }
        });
    }

    private BigDecimal calculateComplianceScore(List<ChecklistAnswerRequestDTO> answers) {
        long answeredItems = answers.stream()
                .filter(answer -> answer.value() != null)
                .count();

        if (answeredItems == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        long compliantItems = answers.stream()
                .filter(answer -> answer.value() == ConformityAnswerValue.COMPLIANT)
                .count();

        return BigDecimal.valueOf(compliantItems)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(answeredItems), 2, RoundingMode.HALF_UP);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void createIssuesForNonCompliantAnswers(
            ChecklistExecution execution,
            List<ChecklistAnswerRequestDTO> answers,
            Map<String, ChecklistItemDTO> itemsByKey
    ) {
        Instant dueAt = Instant.now().plusSeconds(ISSUE_DUE_DAYS * 24L * 60L * 60L);

        answers.stream()
                .filter(answer -> answer.value() == ConformityAnswerValue.NON_COMPLIANT)
                .forEach(answer -> {
                    ChecklistItemDTO item = itemsByKey.get(answer.itemKey());

                    execution.addIssue(ChecklistIssue.builder()
                            .assignedUserReference(new UserReference(execution.getUserId()))
                            .itemKey(answer.itemKey())
                            .itemTitleSnapshot(truncate(item.title(), 150))
                            .title(truncate("Pendencia: " + item.title(), 100))
                            .description(truncate(answer.observation(), 500))
                            .status(IssueStatus.OPEN)
                            .priority(IssuePriority.MEDIUM)
                            .dueAt(dueAt)
                            .build());
                });
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }
}
