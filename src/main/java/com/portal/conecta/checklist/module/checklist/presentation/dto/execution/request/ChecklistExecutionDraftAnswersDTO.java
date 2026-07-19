package com.portal.conecta.checklist.module.checklist.presentation.dto.execution.request;

import com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.submit.SubmitChecklistExecutionCommand;
import jakarta.validation.Valid;

import java.util.List;

/**
 * DTO de entrada para salvar respostas parciais de uma execucao em rascunho (autosave).
 *
 * <p>Diferente de {@link ChecklistExecutionSubmitDTO}, aceita lista vazia — o
 * autosave pode disparar antes de qualquer item ser respondido. A validacao de
 * completude (itens obrigatorios) so acontece no envio final.</p>
 */
public record ChecklistExecutionDraftAnswersDTO(
        @Valid
        List<ChecklistAnswerRequestDTO> answers
) {
    public SubmitChecklistExecutionCommand toCommand() {
        return new SubmitChecklistExecutionCommand(
                answers == null
                        ? List.of()
                        : answers.stream().map((ChecklistAnswerRequestDTO answer) -> answer.toCommand()).toList()
        );
    }
}
