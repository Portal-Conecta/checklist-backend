package com.portal.conecta.checklist.shared.utils;

import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSchema;

import java.util.HashSet;
import java.util.Set;

public class ChecklistSchemaValidator {

    private ChecklistSchemaValidator() {}

    public static void validateStableKeys(ChecklistSchema schemaDTO){
        Set<String> sectionKeys = new HashSet<>();
        Set<String> itemKeys = new HashSet<>();

        schemaDTO.sections().forEach(section -> {
            if (!sectionKeys.add(section.key())){
                throw new IllegalArgumentException("section.key duplicado: " + section.key());
            }
            section.items().forEach(item -> {
                if (!itemKeys.add(item.key())) {
                    throw new IllegalArgumentException("item.key duplicado: " + item.key());
                }
            });
        });
    }
}
