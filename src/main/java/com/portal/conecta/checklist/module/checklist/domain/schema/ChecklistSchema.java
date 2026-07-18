package com.portal.conecta.checklist.module.checklist.domain.schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO raiz do schema de um template de checklist.
 *
 * <p>Organiza as secoes que serao persistidas no template e usadas para validar
 * as respostas no momento do submit.</p>
 */
public record ChecklistSchema(
        @Valid
        @NotEmpty(message = "schemaJson.sections nao pode estar vazio.")
        List<ChecklistSection> sections
) {}
