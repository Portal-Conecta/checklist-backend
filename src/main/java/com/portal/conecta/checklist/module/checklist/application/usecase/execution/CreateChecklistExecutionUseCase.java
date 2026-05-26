package com.portal.conecta.checklist.module.checklist.application.usecase.execution;


import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateChecklistExecutionUseCase {

    private final ChecklistExecutionRepository repository;
    private final ChecklistTemplateRepository templateRepository;
    private final ChecklistExecutionMapper executionMapper;
    private final RequestContextProvider contextProvider;

    @Transactional
    public ChecklistExecution execute(ChecklistExecutionDraftCreateDTO request) {
        ChecklistTemplate template = templateRepository.findById(request.templateId())
                .orElseThrow(() -> new EntityNotFoundException("Template nao encontrado."));

        if (!template.isActive() || template.getStatus() != ChecklistTemplateStatus.ACTIVE) {
            throw new IllegalStateException("Template nao esta ativo.");
        }

        if (!template.getRoomId().equals(request.roomId())) {
            throw new IllegalArgumentException("Template nao pertence a sala informada.");
        }

        RequestContext currentUser = contextProvider.getRequestContext();

        if (!currentUser.canCreateChecklistExecutionForClass(request.classId())) {
            throw new AccessDeniedException("Usuario nao tem permissao para criar checklist para a turma informada.");
        }

        var now = LocalDateTime.now();
        var startOfDay = now.toLocalDate().atStartOfDay();
        var endOfDay = startOfDay.plusDays(1);

        boolean duplicated = repository.existsDuplicateChecklist(
                request.classId(),
                request.roomId(),
                request.period().name(),
                request.checklistType().name(),
                startOfDay,
                endOfDay
        );

        if (duplicated) {
            throw new IllegalArgumentException("Ja existe checklist para esta turma, sala, periodo, dia e tipo.");
        }

        ChecklistExecution execution = executionMapper.toDraftEntity(request, template, currentUser.userId(), now);

        return repository.save(execution);
    }
}
