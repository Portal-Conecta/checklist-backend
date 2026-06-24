package com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.submit;

import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.update.UpdateChecklistAnswerCommand;

import java.util.List;

public record SubmitChecklistExecutionCommand(
        List<UpdateChecklistAnswerCommand> answers
) {
    public SubmitChecklistExecutionCommand {
        answers = answers == null ? List.of() : List.copyOf(answers);
    }
}
