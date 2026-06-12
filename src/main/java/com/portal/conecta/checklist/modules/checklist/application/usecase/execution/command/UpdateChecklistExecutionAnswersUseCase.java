package com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.modules.checklist.application.service.execution.ChecklistExecutionAnswerValidationService;
import com.portal.conecta.checklist.modules.checklist.application.service.execution.ChecklistExecutionDataMapper;
import com.portal.conecta.checklist.modules.checklist.application.service.execution.ChecklistExecutionScoringService;
import com.portal.conecta.checklist.modules.checklist.application.service.execution.ChecklistIssueService;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistExecutionRepositoryPort;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistItem;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSchema;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso responsavel por atualizar respostas de um checklist submetido.
 */
@Service
@RequiredArgsConstructor
public class UpdateChecklistExecutionAnswersUseCase {

    private final ChecklistExecutionRepositoryPort repository;
    private final ChecklistExecutionDataMapper executionMapper;
    private final RequestContextProvider contextProvider;
    private final ChecklistExecutionScoringService scoringService;
    private final ChecklistIssueService issueService;
    private final ObjectMapper objectMapper;
    private final ChecklistExecutionAnswerValidationService answerValidationService;

    @Transactional
    public ChecklistExecution execute(UUID executionId, SubmitChecklistExecutionCommand command) {
        ChecklistExecution execution = repository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execucao de checklist nao encontrada: " + executionId));

        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canManageChecklistTemplates()
                && !currentUser.canOperateChecklistExecutionForClass(execution.getClassId())) {
            throw new AccessDeniedException("Usuario nao tem permissao para editar esta execucao de checklist.");
        }

        if (execution.getStatus() != ChecklistExecutionStatus.SUBMITTED) {
            throw new IllegalStateException("Somente checklists que foram enviados podem ser editados.");
        }

        ChecklistSchema schema = objectMapper.convertValue(
                execution.getChecklistTemplate().getSchemaJson(),
                ChecklistSchema.class
        );

        Map<String, ChecklistItem> itemsByKey = answerValidationService.validate(schema, command.answers());

        issueService.createIssuesForNonCompliantAnswers(execution, command.answers(), itemsByKey);
        execution.setAnswersJson(executionMapper.toAnswersJson(command));
        execution.setComplianceScore(scoringService.calculateComplianceScore(command.answers()));

        return repository.save(execution);
    }
}
