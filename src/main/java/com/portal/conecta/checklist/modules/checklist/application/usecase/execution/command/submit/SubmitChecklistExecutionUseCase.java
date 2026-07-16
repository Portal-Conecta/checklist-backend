package com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.submit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.modules.checklist.application.service.execution.ChecklistExecutionAnswerValidationService;
import com.portal.conecta.checklist.modules.checklist.application.service.execution.ChecklistExecutionDataMapper;
import com.portal.conecta.checklist.modules.checklist.application.service.execution.ChecklistExecutionScoringService;
import com.portal.conecta.checklist.modules.checklist.application.service.execution.ChecklistIssueService;
import com.portal.conecta.checklist.modules.checklist.application.service.window.SubmissionWindowValidator;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistExecutionRepositoryPort;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistItem;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistSchema;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso responsavel por submeter uma execucao de checklist.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmitChecklistExecutionUseCase {

    private final ChecklistExecutionRepositoryPort executionRepository;
    private final ChecklistExecutionDataMapper executionMapper;
    private final ObjectMapper objectMapper;
    private final RequestContextProvider contextProvider;
    private final SubmissionWindowValidator submissionWindowValidator;
    private final ChecklistIssueService issueService;
    private final ChecklistExecutionScoringService scoringService;
    private final ChecklistExecutionAnswerValidationService answerValidationService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${checklist.timezone:America/Sao_Paulo}")
    private String timezone;

    @Transactional
    public ChecklistExecution execute(UUID executionId, SubmitChecklistExecutionCommand command) {
        log.info("Submetendo checklist executionId={}", executionId);

        ChecklistExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execucao de checklist nao encontrada."));

        var currentUser = contextProvider.getRequestContext();

        if (!currentUser.canSubmitChecklistExecutionForClass(execution.getClassId())) {
            throw new AccessDeniedException("Usuario nao tem permissao para enviar esta execucao de checklist.");
        }

        if (execution.getStatus() != ChecklistExecutionStatus.DRAFT) {
            throw new IllegalStateException("Somente checklists em rascunho podem ser enviados.");
        }

        submissionWindowValidator.validate(execution.getClassId(), execution.getChecklistType());

        ChecklistSchema schema = objectMapper.convertValue(
                execution.getChecklistTemplate().getSchemaJson(),
                ChecklistSchema.class
        );
        // TODO: O campo AnswerType foi introduzido no schema (ChecklistItem), mas as validacoes
        // de formato de resposta (TEXT, NUMBER) serao implementadas em uma PR futura.
        // Atualmente, apenas respostas COMPLIANT/NON_COMPLIANT sao validadas.
        Map<String, ChecklistItem> itemsByKey = answerValidationService.validate(schema, command.answers());

        execution.setAnswersJson(executionMapper.toAnswersJson(command));
        execution.setComplianceScore(scoringService.calculateComplianceScore(command.answers()));
        execution.setStatus(ChecklistExecutionStatus.SUBMITTED);
        execution.setSubmittedAt(LocalDateTime.now(ZoneId.of(timezone)));
        execution.setSubmittedBy(currentUser.userId());

        issueService.createIssuesForNonCompliantAnswers(execution, command.answers(), itemsByKey);

        ChecklistExecution submitted = executionRepository.save(execution);
        log.info("Checklist submetido com sucesso executionId={} classId={} score={}%",
                submitted.getId(), submitted.getClassId(), submitted.getComplianceScore());

        if (submitted.getComplianceScore().compareTo(BigDecimal.valueOf(100)) < 0) {
            applicationEventPublisher.publishEvent(new ChecklistNonComplianceEvent(submitted));
        }

        return submitted;
    }
}
