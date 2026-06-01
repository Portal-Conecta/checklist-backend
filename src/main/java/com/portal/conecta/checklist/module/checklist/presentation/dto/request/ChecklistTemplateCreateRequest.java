package com.portal.conecta.checklist.module.checklist.presentation.dto.request;

import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO de entrada para criacao de templates de checklist.
 *
 * <p>Define dados basicos do template e o schema estruturado que sera
 * persistido em JSON para orientar futuras execucoes.</p>
 */
public record ChecklistTemplateCreateRequest(
        @NotNull(message = "roomId e obrigatorio.")
        UUID roomId,

        @NotBlank(message = "title e obrigatorio.")
        @Size(max = 150, message = "title deve ter no maximo 150 caracteres.")
        String title,

        @Size(max = 250, message = "description deve ter no maximo 250 caracteres.")
        String description,

        @Valid
        @NotNull(message = "schemaJson e obrigatorio.")
        ChecklistSchemaDTO schemaJson
) {
}
