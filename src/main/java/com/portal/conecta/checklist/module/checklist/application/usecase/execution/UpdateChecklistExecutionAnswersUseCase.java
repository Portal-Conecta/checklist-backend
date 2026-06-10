package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ChecklistExecutionRepository repository;
    private final ChecklistExecutionMapper executionMapper;
    private final RequestContextProvider contextProvider;
    private final ChecklistExecutionScoringService scoringService;
    private final ChecklistIssueService issueService;
    private final ObjectMapper objectMapper;
    private final ChecklistExecutionAnswerValidationService answerValidationService;

    @Transactional
    public ChecklistExecution execute(UUID executionId, ChecklistExecutionSubmitDTO request) {
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

        ChecklistSchemaDTO schema = objectMapper.convertValue(
                execution.getChecklistTemplate().getSchemaJson(),
                ChecklistSchemaDTO.class
        );

        Map<String, ChecklistItemDTO> itemsByKey = answerValidationService.validate(schema, request.answers());

        issueService.createIssuesForNonCompliantAnswers(execution, request.answers(), itemsByKey);
        execution.setAnswersJson(executionMapper.toAnswersJson(request));
        execution.setComplianceScore(scoringService.calculateComplianceScore(request.answers()));

        return repository.save(execution);
    }
}
