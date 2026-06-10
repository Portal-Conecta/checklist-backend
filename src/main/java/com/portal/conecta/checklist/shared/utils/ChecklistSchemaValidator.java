package com.portal.conecta.checklist.shared.utils;

import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistItemDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;

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

    public static void validateStableKeys(ChecklistSchemaDTO schemaDTO){
        Set<String> sectionKeys = new HashSet<>();
        Set<String> itemKeys = new HashSet<>();

        schemaDTO.sections().forEach(section -> {
            if (!sectionKeys.add(section.key())){
                throw new IllegalArgumentException("section.key duplicado: " + section.key());
            }
            section.items().stream()
                    .map(ChecklistItemDTO::key)
                    .forEach(itemKey -> {
                        if (!itemKeys.add(itemKey)) {
                            throw new IllegalArgumentException("item.key duplicado: " + itemKey);
                        }
                    });
        });
    }
}
