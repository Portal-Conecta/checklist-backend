package com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command;

import java.util.List;

public record SubmitChecklistExecutionCommand(
        List<ChecklistAnswerCommand> answers
) {
    public SubmitChecklistExecutionCommand {
        answers = answers == null ? List.of() : List.copyOf(answers);
    }
}
