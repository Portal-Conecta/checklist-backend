package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.portal.conecta.checklist.module.checklist.domain.enums.ConformityAnswerValue;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistAnswerRequestDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistItemDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSectionDTO;
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

    private ChecklistSchemaDTO schema() {
        return new ChecklistSchemaDTO(List.of(new ChecklistSectionDTO(
                "estrutura",
                "Estrutura",
                1,
                List.of(
                        item("quadro", "Quadro em bom estado?"),
                        item("iluminacao", "Iluminacao adequada?")
                )
        )));
    }

    private ChecklistItemDTO item(String key, String title) {
        return new ChecklistItemDTO(key, title, null, true, 1);
    }

    private ChecklistAnswerRequestDTO answer(
            String itemKey,
            ConformityAnswerValue value,
            String observation
    ) {
        return new ChecklistAnswerRequestDTO(itemKey, value, observation, Instant.now());
    }
}
