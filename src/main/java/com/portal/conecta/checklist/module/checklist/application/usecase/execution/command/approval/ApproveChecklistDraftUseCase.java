package com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.approval;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistApprovalStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApproveChecklistDraftUseCase {

    private final ChecklistExecutionRepository checklistExecutionRepository;
    private final RequestContextProvider requestContextProvider;

    @Transactional
    public ChecklistExecution execution(UUID executionId){
        ChecklistExecution execution = checklistExecutionRepository.findById(executionId)
                .orElseThrow(() -> new EntityNotFoundException("Execução de checklist não encontrada "));


        if(execution.getChecklistApprovalStatus() != ChecklistApprovalStatus.PENDING){
            throw new IllegalStateException("Apenas checklists pedentes podem ser apravados");
        }
        RequestContext context = requestContextProvider.getRequestContext();

        if(!context.isTeacherOfClass(execution.getClassId())){
            throw new AccessDeniedException("apenas professores da turma podem aprovar a realizacao desse checklist");
        }

        execution.setChecklistApprovalStatus(ChecklistApprovalStatus.APPROVED);
        execution.setApprovedBy(context.getUserId());
        execution.setApprovedAt(LocalDateTime.now());
        return checklistExecutionRepository.save(execution);

    }


}
