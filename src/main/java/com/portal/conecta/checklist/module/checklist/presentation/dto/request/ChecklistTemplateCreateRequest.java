package com.portal.conecta.checklist.module.checklist.presentation.dto.request;

import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

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
        @Schema(description = "Schema do checklist. Limites: ate 20 secoes, ate 50 itens por secao e ate 500 itens no total.")
        ChecklistSchemaDTO schemaJson
) {
}
