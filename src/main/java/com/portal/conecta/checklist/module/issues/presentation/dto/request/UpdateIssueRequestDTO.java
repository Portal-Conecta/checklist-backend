package com.portal.conecta.checklist.module.issues.presentation.dto.request;

import com.portal.conecta.checklist.module.issues.presentation.dto.enums.IssuePriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Dados para atualização de uma pendência existente — todos os campos são opcionais (partial update)")
public record UpdateIssueRequestDTO(

        @Schema(description = "Novo responsável pela resolução", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID assignedTo,

        @Schema(description = "Novo título da pendência", example = "EPI ausente — reincidência")
        @Size(min = 5, max = 150, message = "O título deve ter entre 5 e 150 caracteres.")
        String title,

        @Schema(description = "Nova descrição da pendência")
        @Size(max = 2000, message = "A descrição não pode ultrapassar 2000 caracteres.")
        String description,

        @Schema(description = "Nova prioridade da pendência", example = "CRITICAL")
        IssuePriority priority,

        @Schema(description = "Novo prazo para resolução", example = "2026-07-15T18:00:00")
        @Future(message = "O prazo deve ser uma data futura.")
        LocalDateTime dueAt
) {
}