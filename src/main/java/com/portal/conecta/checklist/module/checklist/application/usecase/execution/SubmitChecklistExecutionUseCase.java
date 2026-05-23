package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistExecutionStatus;
import com.portal.conecta.checklist.module.checklist.domain.enums.ConformityAnswerValue;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import com.portal.conecta.checklist.module.issues.domain.model.ChecklistIssue;
import com.portal.conecta.checklist.module.issues.presentation.mapper.ChecklistIssueMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubmitChecklistExecutionUseCase {

    private final ChecklistExecutionRepository repository;
    private final ChecklistExecutionMapper executionMapper;
    private final ChecklistIssueMapper issueMapper;


    public ChecklistExecution execute(UUID executionId, ChecklistExecutionSubmitDTO submitDTO) {

        ChecklistExecution execution = repository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Checklist não encontrada."));

        if (execution.getStatus() != ChecklistExecutionStatus.DRAFT) {
            throw new IllegalStateException("Apenas checklist com status DRAFT podem ser finalizadas");
        }

        execution.setAnswersJson(executionMapper.toAnswersJson(submitDTO));

        submitDTO.answers().stream()
                .filter(answer -> answer.value() == ConformityAnswerValue.NON_COMPLIANT)
                .map(issueMapper::toEntity)
                .forEach(execution::addIssue);



        execution.setStatus(ChecklistExecutionStatus.SUBMITTED);
        execution.setSubmittedAt(LocalDateTime.now());

        return repository.save(execution);
    }
}
