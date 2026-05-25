package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.enums.ConformityAnswerValue;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistAnswerRequestDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistItemDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    private final ChecklistExecutionRepository executionRepository;
    private final ChecklistExecutionMapper executionMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public ChecklistExecution execute(UUID executionId, ChecklistExecutionSubmitDTO request) {
        ChecklistExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execucao de checklist nao encontrada."));

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
}
