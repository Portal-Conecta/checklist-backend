package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.enums.ConformityAnswerValue;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecutionDraft;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.UserReference;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.module.checklist.infrastructure.redis.ChecklistDraftRedisRepository;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistAnswerResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistItemDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import com.portal.conecta.checklist.module.issues.domain.enums.IssuePriority;
import com.portal.conecta.checklist.module.issues.domain.enums.IssueStatus;
import com.portal.conecta.checklist.module.issues.domain.model.ChecklistIssue;
import com.portal.conecta.checklist.module.checklist.application.usecase.window.SubmissionWindowValidator;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Caso de uso responsavel por submeter uma execucao de checklist baseando-se no cache do Redis.
 */
@Service
@RequiredArgsConstructor
public class SubmitChecklistExecutionUseCase {

    private static final int ISSUE_DUE_DAYS = 7;

    private final ChecklistExecutionRepository executionRepository;
    private final ChecklistDraftRedisRepository draftRedisRepository;
    private final ChecklistExecutionMapper executionMapper;
    private final ObjectMapper objectMapper;
    private final RequestContextProvider contextProvider;
    private final SubmissionWindowValidator submissionWindowValidator;

    @Value("${checklist.timezone:America/Sao_Paulo}")
    private String timezone;

    @Transactional
    public ChecklistExecution execute(UUID executionId) {
        ChecklistExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execucao de checklist nao encontrada."));

        var currentUser = contextProvider.getRequestContext();

        if (!execution.getUserId().equals(currentUser.userId())
                || !currentUser.canSubmitChecklistExecutionForClass(execution.getClassId())) {
            throw new AccessDeniedException("Usuario nao tem permissao para enviar esta execucao de checklist.");
        }

        if (execution.getStatus() != ChecklistExecutionStatus.DRAFT) {
            throw new IllegalStateException("Somente checklists em rascunho podem ser enviados.");
        }

        submissionWindowValidator.validate(execution.getClassId(), execution.getChecklistType());

        List<ChecklistAnswerResponseDTO> draftAnswers = draftRedisRepository.findByExecutionId(executionId)
                .map(ChecklistExecutionDraft::getAnswers)
                .orElse(List.of());

        ChecklistSchemaDTO schema = objectMapper.convertValue(
                execution.getChecklistTemplate().getSchemaJson(),
                ChecklistSchemaDTO.class
        );

        Map<String, ChecklistItemDTO> itemsByKey = itemsByKey(schema);
        Map<String, ChecklistAnswerResponseDTO> answersByItemKey = answersByItemKey(draftAnswers);

        validateAnswers(itemsByKey, answersByItemKey);

        execution.setAnswersJson(executionMapper.toAnswersJson(draftAnswers));
        execution.setComplianceScore(calculateComplianceScore(draftAnswers));
        execution.setStatus(ChecklistExecutionStatus.SUBMITTED);
        execution.setSubmittedAt(LocalDateTime.now(ZoneId.of(timezone)));
        createIssuesForNonCompliantAnswers(execution, draftAnswers, itemsByKey);

        ChecklistExecution savedExecution = executionRepository.save(execution);

        draftRedisRepository.deleteByExecutionId(executionId);

        return savedExecution;
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

    private Map<String, ChecklistAnswerResponseDTO> answersByItemKey(List<ChecklistAnswerResponseDTO> answers) {
        return answers.stream()
                .collect(Collectors.toMap(
                        ChecklistAnswerResponseDTO::itemKey,
                        Function.identity(),
                        (first, duplicated) -> {
                            throw new IllegalArgumentException("Resposta duplicada para itemKey: " + first.itemKey());
                        },
                        LinkedHashMap::new
                ));
    }

    private void validateAnswers(
            Map<String, ChecklistItemDTO> itemsByKey,
            Map<String, ChecklistAnswerResponseDTO> answersByItemKey
    ) {
        for (String answerItemKey : answersByItemKey.keySet()) {
            if (!itemsByKey.containsKey(answerItemKey)) {
                throw new IllegalArgumentException("Resposta enviada para item inexistente no template: " + answerItemKey);
            }
        }

        for (ChecklistItemDTO item : itemsByKey.values()) {
            ChecklistAnswerResponseDTO answer = answersByItemKey.get(item.key());

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

    private BigDecimal calculateComplianceScore(List<ChecklistAnswerResponseDTO> answers) {
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
            List<ChecklistAnswerResponseDTO> answers,
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