package com.portal.conecta.checklist.module.checklist.presentation.dto.checklistTemplate;

import com.portal.conecta.checklist.module.checklist.presentation.dto.UserToken;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ChecklistTemplateCreateDTO(
        @NotNull(message = "O ID do quarto é obrigatório")
        UUID roomId,

        @NotBlank(message = "O título é obrigatório")
        @Size(max = 150, message = "O título deve ter no máximo 150 caracteres")
        String title,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 250, message = "A descrição deve ter no máximo 250 caracteres")
        String description,

        @NotNull(message = "O schema JSON é obrigatório")
        UserToken schemaJson
        ) {
    }
