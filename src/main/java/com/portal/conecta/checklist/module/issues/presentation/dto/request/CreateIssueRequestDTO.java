package com.portal.conecta.checklist.module.issues.presentation.dto.request;

import com.portal.conecta.checklist.module.issues.presentation.dto.enums.IssuePriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Dados para criação de uma nova pendência a partir de um item não conforme")
public record CreateIssueRequestDTO(

        @Schema(description = "ID da execução do checklist que originou esta pendência", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull(message = "O ID da execução é obrigatório.")
        UUID executionId,

        @Schema(description = "Chave do item dentro do schema_json/answers_json que gerou a pendência", example = "item_007")
        @NotBlank(message = "A chave do item é obrigatória.")
        @Size(max = 255, message = "A chave do item não pode ultrapassar 255 caracteres.")
        String itemKey,

        @Schema(description = "Título do item no momento em que a pendência foi criada (snapshot)", example = "Uso de EPI obrigatório")
        @NotBlank(message = "O título snapshot do item é obrigatório.")
        @Size(max = 255, message = "O título snapshot não pode ultrapassar 255 caracteres.")
        String itemTitleSnapshot,

        @Schema(description = "ID do usuário responsável pela resolução", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull(message = "O responsável pela resolução é obrigatório.")
        UUID assignedTo,

        @Schema(description = "Título da pendência", example = "EPI ausente no posto de trabalho")
        @NotBlank(message = "O título da pendência é obrigatório.")
        @Size(min = 5, max = 150, message = "O título deve ter entre 5 e 150 caracteres.")
        String title,

        @Schema(description = "Descrição detalhada da pendência", example = "Colaborador não utilizava capacete durante atividade de risco identificada na inspeção.")
        @NotBlank(message = "A descrição da pendência é obrigatória.")
        @Size(max = 2000, message = "A descrição não pode ultrapassar 2000 caracteres.")
        String description,

        @Schema(description = "Prioridade da pendência", example = "HIGH")
        @NotNull(message = "A prioridade é obrigatória.")
        IssuePriority priority,

        @Schema(description = "Prazo para resolução da pendência", example = "2026-06-30T18:00:00")
        @NotNull(message = "O prazo de resolução é obrigatório.")
        @Future(message = "O prazo deve ser uma data futura.")
        LocalDateTime dueAt
) {
}