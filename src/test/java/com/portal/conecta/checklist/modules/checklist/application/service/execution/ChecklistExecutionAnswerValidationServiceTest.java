package com.portal.conecta.checklist.modules.checklist.application.service.execution;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ConformityAnswerValue;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.ChecklistAnswerCommand;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistItem;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSchema;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSection;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChecklistExecutionAnswerValidationServiceTest {

    private final ChecklistExecutionAnswerValidationService service = new ChecklistExecutionAnswerValidationService();

    @Test
    void shouldReturnItemsWhenAnswersAreValid() {
        var items = service.validate(schema(), List.of(
                answer("quadro", ConformityAnswerValue.COMPLIANT, null),
                answer("iluminacao", ConformityAnswerValue.NON_COMPLIANT, "Lampada queimada")));

        assertThat(items).containsKeys("quadro", "iluminacao");
    }

    @Test
    void shouldRejectAnswerForUnknownItem() {
        var answers = List.of(
                answer("quadro", ConformityAnswerValue.COMPLIANT, null),
                answer("porta", ConformityAnswerValue.COMPLIANT, null));

        assertThrows(IllegalArgumentException.class, () -> service.validate(schema(), answers));
    }

    @Test
    void shouldRejectMissingRequiredAnswer() {
        var answers = List.of(answer("quadro", ConformityAnswerValue.COMPLIANT, null));

        assertThrows(IllegalArgumentException.class, () -> service.validate(schema(), answers));
    }

    @Test
    void shouldRejectDuplicatedAnswer() {
        var answers = List.of(
                answer("quadro", ConformityAnswerValue.COMPLIANT, null),
                answer("quadro", ConformityAnswerValue.NON_COMPLIANT, "Riscado"));

        assertThrows(IllegalArgumentException.class, () -> service.validate(schema(), answers));
    }

    @Test
    void shouldRejectNonCompliantAnswerWithoutObservation() {
        var answers = List.of(
                answer("quadro", ConformityAnswerValue.COMPLIANT, null),
                answer("iluminacao", ConformityAnswerValue.NON_COMPLIANT, " "));

        assertThrows(IllegalArgumentException.class, () -> service.validate(schema(), answers));
    }

    private ChecklistSchema schema() {
        return new ChecklistSchema(List.of(new ChecklistSection(
                "estrutura",
                "Estrutura",
                1,
                List.of(
                        item("quadro", "Quadro em bom estado?"),
                        item("iluminacao", "Iluminacao adequada?")))));
    }

    private ChecklistItem item(String key, String title) {
        return new ChecklistItem(key, title, null, true, 1);
    }

    private ChecklistAnswerCommand answer(
            String itemKey,
            ConformityAnswerValue value,
            String observation) {
        return new ChecklistAnswerCommand(itemKey, value, observation, Instant.now());
    }
}
