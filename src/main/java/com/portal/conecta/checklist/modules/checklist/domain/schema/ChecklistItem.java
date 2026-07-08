package com.portal.conecta.checklist.modules.checklist.domain.schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO que descreve um item dentro do schema de checklist.
 *
 * <p>Define chave estavel, titulo, descricao, obrigatoriedade e ordem de
 * exibicao usados durante a execucao.</p>
 */
public record ChecklistItem(
        @NotBlank(message = "item.key e obrigatorio.")
        String key,

        @NotBlank(message = "item.title e obrigatorio.")
        @Size(max = 150, message = "item.title deve ter no maximo 150 caracteres.")
        String title,

        @Size(max = 250, message = "item.description deve ter no maximo 250 caracteres.")
        String description,

        @NotNull(message = "item.required e obrigatorio.")
        Boolean required,

        @NotNull(message = "item.order e obrigatorio.")
        Integer order,

        @Size(max = 100, message = "item.category deve ter no maximo 100 caracteres.")
        String category
) {
    public ChecklistItem(String key, String title, String description, Boolean required, Integer order) {
        this(key, title, description, required, order, null);
    }
}
