package com.portal.conecta.checklist.modules.checklist.presentation.dto.execution.request;

import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.SubmitChecklistExecutionCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO de entrada para submissao de uma execucao de checklist.
 *
 * <p>A lista de respostas e validada contra o schema do template antes de o
 * checklist sair do status de rascunho.</p>
 */
public record ChecklistExecutionSubmitDTO(
        @Valid
        @NotEmpty(message = "answers nao pode estar vazio.")
        List<ChecklistAnswerRequestDTO> answers
) {
    public SubmitChecklistExecutionCommand toCommand() {
        return new SubmitChecklistExecutionCommand(
                answers == null
                        ? List.of()
                        : answers.stream().map(ChecklistAnswerRequestDTO::toCommand).toList()
        );
    }
}
