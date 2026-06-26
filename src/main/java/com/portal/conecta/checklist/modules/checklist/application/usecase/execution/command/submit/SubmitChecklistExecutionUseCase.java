package com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.submit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.modules.checklist.application.port.out.messaging.NotificationEventPublisher; // INJETADO
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
import com.portal.conecta.checklist.shared.messaging.event.NotificationEvent; // IMPORT DO EVENTO
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso responsavel por submeter uma execucao de checklist.
 */
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
    private final NotificationEventPublisher notificationPublisher;

    @Value("${checklist.timezone:America/Sao_Paulo}")
    private String timezone;

    @Transactional
    public ChecklistExecution execute(UUID executionId, SubmitChecklistExecutionCommand command) {
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

        ChecklistSchema schema = objectMapper.convertValue(
                execution.getChecklistTemplate().getSchemaJson(),
                ChecklistSchema.class
        );

        Map<String, ChecklistItem> itemsByKey = answerValidationService.validate(schema, command.answers());

        execution.setAnswersJson(executionMapper.toAnswersJson(command));
        execution.setComplianceScore(scoringService.calculateComplianceScore(command.answers()));
        execution.setStatus(ChecklistExecutionStatus.SUBMITTED);
        execution.setSubmittedAt(LocalDateTime.now(ZoneId.of(timezone)));

         issueService.createIssuesForNonCompliantAnswers(execution, command.answers(), itemsByKey);

        ChecklistExecution savedExecution = executionRepository.save(execution);

        if (savedExecution.getComplianceScore().compareTo(new java.math.BigDecimal("100")) < 0) {
            publishNonComplianceNotification(savedExecution);
        }

        return savedExecution;
    }

    private void publishNonComplianceNotification(ChecklistExecution execution) {
        var filters = List.of(new NotificationEvent.NotificationFilter("ROLE", "WEG"));

        var scope = List.of(new NotificationEvent.NotificationScope("CLASS", execution.getClassId().toString()));

        Map<String, Object> metadata = Map.of(
                "executionId", execution.getId().toString(),
                "classId", execution.getClassId().toString(),
                "score", execution.getComplianceScore(),
                "route", "/turmas/" + execution.getClassId() + "/checklists/" + execution.getId()
        );

        NotificationEvent event = new NotificationEvent(
                UUID.randomUUID().toString(),               // messageId único
                execution.getId().toString(),               // correlationId
                "checklist-service",                        // source
                "checklist.non_compliance.created",         // eventType de não conformidade
                Instant.now(),                              // occurredAt (UTC)
                "Não Conformidade Identificada",             // Título da notificação
                "Um checklist foi submetido com itens não conformes na turma. Pontuação: " + execution.getComplianceScore() + "%.",
                filters,
                scope,
                metadata
        );

        notificationPublisher.publish(event);
    }


}