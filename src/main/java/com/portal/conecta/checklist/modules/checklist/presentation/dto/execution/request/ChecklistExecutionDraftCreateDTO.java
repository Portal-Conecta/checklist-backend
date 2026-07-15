package com.portal.conecta.checklist.modules.checklist.presentation.dto.execution.request;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.create.CreateChecklistExecutionCommand;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO de entrada para criacao de uma execucao de checklist em rascunho.
 *
 * <p>O campo {@code period} foi removido intencionalmente — ele e derivado
 * server-side via {@code PeriodResolver} a partir do shift da turma, eliminando
 * a possibilidade de duplicatas silenciosas causadas por period declarado
 * incorretamente pelo cliente (RISK-001).</p>
 */
public record ChecklistExecutionDraftCreateDTO(
        @NotNull(message = "templateId e obrigatorio.")
        UUID templateId,

        @NotNull(message = "classId e obrigatorio.")
        UUID classId,

        @NotNull(message = "checklistType e obrigatorio.")
        ChecklistType checklistType
) {
    public CreateChecklistExecutionCommand toCommand() {
        return new CreateChecklistExecutionCommand(templateId, classId, checklistType);
    }
}
