package com.portal.conecta.checklist.shared.utils;

import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistItem;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSchema;

import java.util.HashSet;
import java.util.Set;

/**
 * Utilitário de validação do schema de checklist.
 *
 * <p>Centraliza regras de validação estrutural do schema que são
 * compartilhadas entre múltiplos casos de uso.</p>
 */
public class ChecklistSchemaValidator {

    private ChecklistSchemaValidator() {}

    public static void validateStableKeys(ChecklistSchema schemaDTO){
        Set<String> sectionKeys = new HashSet<>();
        Set<String> itemKeys = new HashSet<>();

        schemaDTO.sections().forEach(section -> {
            if (!sectionKeys.add(section.key())){
                throw new IllegalArgumentException("section.key duplicado: " + section.key());
            }
            section.items().stream()
                    .map(item -> item.key())
                    .forEach(itemKey -> {
                        if (!itemKeys.add(itemKey)) {
                            throw new IllegalArgumentException("item.key duplicado: " + itemKey);
                        }
                    });
        });
    }
}
