package com.portal.conecta.checklist.module.checklist.domain.schema;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO que representa uma secao do schema de checklist.
 *
 * <p>Agrupa itens relacionados por chave e ordem, permitindo construir
 * formularios de verificacao organizados por blocos.</p>
 */
public record ChecklistSection(
        @NotBlank(message = "section.key e obrigatorio.")
        String key,

        @NotBlank(message = "section.title e obrigatorio.")
        String title,

        @NotNull(message = "section.order e obrigatorio.")
        Integer order,

        @Valid
        @NotEmpty(message = "section.items nao pode estar vazio.")
        List<ChecklistItem> items
) {}
