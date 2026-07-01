package com.portal.conecta.checklist.modules.checklist.application.service.execution;

import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.ChecklistAnswerCommand;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ConformityAnswerValue;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistItem;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSchema;
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

    public Map<String, ChecklistItem> validate(
            ChecklistSchema schema,
            List<ChecklistAnswerCommand> answers
    ) {
        Map<String, ChecklistItem> itemsByKey = itemsByKey(schema);
        Map<String, ChecklistAnswerCommand> answersByItemKey = answersByItemKey(answers);

        validateAnswers(itemsByKey, answersByItemKey);

        return itemsByKey;
    }

    private Map<String, ChecklistItem> itemsByKey(ChecklistSchema schema) {
        return schema.sections().stream()
                .flatMap(section -> section.items().stream())
                .collect(Collectors.toMap(
                        item -> item.key(),
                        Function.identity(),
                        (first, duplicated) -> {
                            throw new IllegalArgumentException("item.key duplicado no template: " + first.key());
                        },
                        LinkedHashMap::new
                ));
    }

    private Map<String, ChecklistAnswerCommand> answersByItemKey(List<ChecklistAnswerCommand> answers) {
        return answers.stream()
                .collect(Collectors.toMap(
                    answer -> answer.itemKey(),
                        Function.identity(),
                        (first, duplicated) -> {
                            throw new IllegalArgumentException("Resposta duplicada para itemKey: " + first.itemKey());
                        },
                        LinkedHashMap::new
                ));
    }

    private void validateAnswers(
            Map<String, ChecklistItem> itemsByKey,
            Map<String, ChecklistAnswerCommand> answersByItemKey
    ) {
        for (String answerItemKey : answersByItemKey.keySet()) {
            if (!itemsByKey.containsKey(answerItemKey)) {
                throw new IllegalArgumentException("Resposta enviada para item inexistente no template: " + answerItemKey);
            }
        }

        for (ChecklistItem item : itemsByKey.values()) {
            ChecklistAnswerCommand answer = answersByItemKey.get(item.key());

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
