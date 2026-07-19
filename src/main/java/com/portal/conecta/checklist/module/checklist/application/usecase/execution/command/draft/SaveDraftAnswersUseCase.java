package com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.draft;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.application.service.execution.ChecklistExecutionAnswerValidationService;
import com.portal.conecta.checklist.module.checklist.application.service.execution.ChecklistExecutionDataMapper;
import com.portal.conecta.checklist.module.checklist.application.service.execution.ChecklistExecutionScoringService;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.submit.SubmitChecklistExecutionCommand;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.application.port.out.persistence.ChecklistExecutionRepositoryPort;
import com.portal.conecta.checklist.module.checklist.domain.schema.ChecklistSchema;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso responsavel por salvar respostas parciais de uma execucao em
 * rascunho (autosave), sem exigir completude nem mudar o status.
 *
 * <p>Nao cria issues — pendencias so nascem no envio final
 * ({@code SubmitChecklistExecutionUseCase}), ja que a resposta ainda pode
 * mudar antes de o usuario confirmar o envio.</p>
 */
@Service
@RequiredArgsConstructor
public class SaveDraftAnswersUseCase {

    private final ChecklistExecutionRepositoryPort repository;
    private final ChecklistExecutionDataMapper executionMapper;
    private final ObjectMapper objectMapper;
    private final RequestContextProvider contextProvider;
    private final ChecklistExecutionScoringService scoringService;
    private final ChecklistExecutionAnswerValidationService answerValidationService;

    @Transactional
    public ChecklistExecution execute(UUID executionId, SubmitChecklistExecutionCommand command) {
        ChecklistExecution execution = repository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execucao de checklist nao encontrada."));

        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canOperateChecklistExecutionForClass(execution.getClassId())) {
            throw new AccessDeniedException("Usuario nao tem permissao para salvar respostas desta execucao de checklist.");
        }

        if (execution.getStatus() != ChecklistExecutionStatus.DRAFT) {
            throw new IllegalStateException("Somente checklists em rascunho podem ter respostas salvas parcialmente.");
        }

        ChecklistSchema schema = objectMapper.convertValue(
                execution.getChecklistTemplate().getSchemaJson(),
                ChecklistSchema.class
        );
        answerValidationService.validatePartial(schema, command.answers());

        execution.setAnswersJson(executionMapper.toAnswersJson(command));
        execution.setComplianceScore(scoringService.calculateComplianceScore(command.answers()));

        return repository.save(execution);
    }
}
