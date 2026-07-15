package com.portal.conecta.checklist.shared.utils;

import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistItem;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSchema;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChecklistSchemaValidatorTest {

    @Test
    @DisplayName("deve validar schema com chaves unicas")
    void deveValidarSchemaComChavesUnicas() {
        ChecklistSchema schema = new ChecklistSchema(
                List.of(
                        new ChecklistSection("section-1", "Section 1", 1, List.of(new ChecklistItem("item-1", "Item 1", null, true, 1))),
                        new ChecklistSection("section-2", "Section 2", 2, List.of(new ChecklistItem("item-2", "Item 2", null, true, 1)))
                )
        );

        assertThatCode(() -> ChecklistSchemaValidator.validateStableKeys(schema))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("deve lancar excecao quando houver secao duplicada")
    void deveLancarExcecaoQuandoHouverSecaoDuplicada() {
        ChecklistSchema schema = new ChecklistSchema(
                List.of(
                        new ChecklistSection("section-1", "Section 1", 1, List.of(new ChecklistItem("item-1", "Item 1", null, true, 1))),
                        new ChecklistSection("section-1", "Section 1", 2, List.of(new ChecklistItem("item-2", "Item 2", null, true, 1)))
                )
        );

        assertThatThrownBy(() -> ChecklistSchemaValidator.validateStableKeys(schema))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("section.key duplicado: section-1");
    }

    @Test
    @DisplayName("deve lancar excecao quando houver item duplicado entre secoes")
    void deveLancarExcecaoQuandoHouverItemDuplicadoEntreSecoes() {
        ChecklistSchema schema = new ChecklistSchema(
                List.of(
                        new ChecklistSection("section-1", "Section 1", 1, List.of(new ChecklistItem("item-1", "Item 1", null, true, 1))),
                        new ChecklistSection("section-2", "Section 2", 2, List.of(new ChecklistItem("item-1", "Item 1", null, true, 1)))
                )
        );

        assertThatThrownBy(() -> ChecklistSchemaValidator.validateStableKeys(schema))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("item.key duplicado: item-1");
    }
}
