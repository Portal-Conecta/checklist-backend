package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.application.usecase.window.SubmissionWindowValidator;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistItemDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso responsavel por submeter uma execucao de checklist.
 */
@Service
@RequiredArgsConstructor
public class SubmitChecklistExecutionUseCase {

    private final ChecklistExecutionRepository executionRepository;
    private final ChecklistExecutionMapper executionMapper;
    private final ObjectMapper objectMapper;
    private final RequestContextProvider contextProvider;
    private final SubmissionWindowValidator submissionWindowValidator;
    private final ChecklistIssueService issueService;
    private final ChecklistExecutionScoringService scoringService;
    private final ChecklistExecutionAnswerValidationService answerValidationService;

    @Value("${checklist.timezone:America/Sao_Paulo}")
    private String timezone;

    @Transactional
    public ChecklistExecution execute(UUID executionId, ChecklistExecutionSubmitDTO request) {
        ChecklistExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execucao de checklist nao encontrada."));

        var currentUser = contextProvider.getRequestContext();

        if (!execution.getUserId().equals(currentUser.userId())
                || !currentUser.canSubmitChecklistExecutionForClass(execution.getClassId())) {
            throw new AccessDeniedException("Usuario nao tem permissao para enviar esta execucao de checklist.");
        }

        if (execution.getStatus() != ChecklistExecutionStatus.DRAFT) {
            throw new IllegalStateException("Somente checklists em rascunho podem ser enviados.");
        }

        submissionWindowValidator.validate(execution.getClassId(), execution.getChecklistType());

        ChecklistSchemaDTO schema = objectMapper.convertValue(
                execution.getChecklistTemplate().getSchemaJson(),
                ChecklistSchemaDTO.class
        );

        Map<String, ChecklistItemDTO> itemsByKey = answerValidationService.validate(schema, request.answers());

        execution.setAnswersJson(executionMapper.toAnswersJson(request));
        execution.setComplianceScore(scoringService.calculateComplianceScore(request.answers()));
        execution.setStatus(ChecklistExecutionStatus.SUBMITTED);
        execution.setSubmittedAt(LocalDateTime.now(ZoneId.of(timezone)));
        issueService.createIssuesForNonCompliantAnswers(execution, request.answers(), itemsByKey);

        return executionRepository.save(execution);
    }
}
