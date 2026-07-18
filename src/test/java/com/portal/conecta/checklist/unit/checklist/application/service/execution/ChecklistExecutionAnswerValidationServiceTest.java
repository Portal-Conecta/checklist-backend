package com.portal.conecta.checklist.unit.checklist.application.service.execution;

import com.portal.conecta.checklist.module.checklist.application.service.execution.ChecklistExecutionAnswerValidationService;
import com.portal.conecta.checklist.module.checklist.domain.enums.AnswerType;
import com.portal.conecta.checklist.module.checklist.domain.enums.ConformityAnswerValue;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.update.UpdateChecklistAnswerCommand;
import com.portal.conecta.checklist.module.checklist.domain.schema.ChecklistItem;
import com.portal.conecta.checklist.module.checklist.domain.schema.ChecklistSchema;
import com.portal.conecta.checklist.module.checklist.domain.schema.ChecklistSection;
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
                answer("iluminacao", ConformityAnswerValue.NON_COMPLIANT, "Lampada queimada")
        ));

        assertThat(items).containsKeys("quadro", "iluminacao");
    }

    @Test
    void shouldRejectAnswerForUnknownItem() {
        var answers = List.of(
                answer("quadro", ConformityAnswerValue.COMPLIANT, null),
                answer("porta", ConformityAnswerValue.COMPLIANT, null)
        );

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
                answer("quadro", ConformityAnswerValue.NON_COMPLIANT, "Riscado")
        );

        assertThrows(IllegalArgumentException.class, () -> service.validate(schema(), answers));
    }

    @Test
    void shouldRejectNonCompliantAnswerWithoutObservation() {
        var answers = List.of(
                answer("quadro", ConformityAnswerValue.COMPLIANT, null),
                answer("iluminacao", ConformityAnswerValue.NON_COMPLIANT, " ")
        );

        assertThrows(IllegalArgumentException.class, () -> service.validate(schema(), answers));
    }

    private ChecklistSchema schema() {
        return new ChecklistSchema(List.of(new ChecklistSection(
                "estrutura",
                "Estrutura",
                1,
                List.of(
                        item("quadro", "Quadro em bom estado?"),
                        item("iluminacao", "Iluminacao adequada?")
                )
        )));
    }

    private ChecklistItem item(String key, String title) {
        return new ChecklistItem(key, title, null, AnswerType.CONFORMITY, true, 1);
    }

    private UpdateChecklistAnswerCommand answer(
            String itemKey,
            ConformityAnswerValue value,
            String observation
    ) {
        return new UpdateChecklistAnswerCommand(itemKey, value, observation, Instant.now());
    }
}
