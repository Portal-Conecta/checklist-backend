package com.portal.conecta.checklist.module.issues.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para registrar a resolução de uma pendência — muda o status para RESOLVED e preenche resolved_at")
public record ResolveIssueRequestDTO(

        @Schema(description = "Descrição das ações tomadas para resolver a pendência", example = "EPIs foram fornecidos e colaborador orientado. Registro fotográfico anexado.")
        @NotBlank(message = "A descrição da resolução é obrigatória.")
        @Size(min = 10, max = 2000, message = "A resolução deve ter entre 10 e 2000 caracteres.")
        String resolutionNotes
) {
}