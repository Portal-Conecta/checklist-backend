package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.portal.conecta.checklist.module.checklist.domain.enums.ConformityAnswerValue;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistAnswerRequestDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistItemDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Valida respostas de uma execucao contra o schema do template.
 */
@Service
public class ChecklistExecutionAnswerValidationService {

    public Map<String, ChecklistItemDTO> validate(
            ChecklistSchemaDTO schema,
            List<ChecklistAnswerRequestDTO> answers
    ) {
        Map<String, ChecklistItemDTO> itemsByKey = itemsByKey(schema);
        Map<String, ChecklistAnswerRequestDTO> answersByItemKey = answersByItemKey(answers);

        validateAnswers(itemsByKey, answersByItemKey);

        return itemsByKey;
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
