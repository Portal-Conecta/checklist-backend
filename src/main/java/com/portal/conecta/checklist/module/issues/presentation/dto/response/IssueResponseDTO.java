package com.portal.conecta.checklist.module.issues.presentation.dto.response;

import com.portal.conecta.checklist.module.issues.presentation.dto.enums.IssuePriority;
import com.portal.conecta.checklist.module.issues.presentation.dto.enums.IssueStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Representação completa de uma pendência")
public record IssueResponseDTO(

        @Schema(description = "Identificador único da pendência")
        UUID id,

        @Schema(description = "ID da execução do checklist que originou esta pendência")
        UUID executionId,

        @Schema(description = "Chave do item dentro do schema_json/answers_json")
        String itemKey,

        @Schema(description = "Título do item no momento em que a pendência foi criada")
        String itemTitleSnapshot,

        @Schema(description = "ID do usuário responsável pela resolução")
        UUID assignedTo,

        @Schema(description = "Título da pendência")
        String title,

        @Schema(description = "Descrição detalhada da pendência")
        String description,

        @Schema(description = "Estado atual da pendência")
        IssueStatus status,

        @Schema(description = "Prioridade da pendência")
        IssuePriority priority,

        @Schema(description = "Prazo para resolução")
        LocalDateTime dueAt,

        @Schema(description = "Data e hora de resolução — preenchido ao mudar para RESOLVED")
        LocalDateTime resolvedAt,

        @Schema(description = "Data e hora de criação do registro")
        LocalDateTime createdAt,

        @Schema(description = "Data e hora da última atualização")
        LocalDateTime updatedAt
) {
}