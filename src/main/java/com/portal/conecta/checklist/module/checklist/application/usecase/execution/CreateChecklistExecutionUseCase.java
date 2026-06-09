package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.portal.conecta.checklist.module.checklist.application.usecase.window.SubmissionWindowValidator;
import com.portal.conecta.checklist.module.checklist.domain.PeriodResolver;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.module.checklist.domain.enums.Period;
import com.portal.conecta.checklist.module.checklist.domain.enums.Shift;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.ClassReference;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.CourseReference;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.hub.provider.classes.HubClassProvider;
import com.portal.conecta.checklist.shared.hub.provider.course.HubCourseProvider;
import com.portal.conecta.checklist.shared.hub.provider.room.HubRoomProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class CreateChecklistExecutionUseCase {

    private final ChecklistExecutionRepository repository;
    private final ChecklistTemplateRepository templateRepository;
    private final ChecklistExecutionMapper executionMapper;
    private final RequestContextProvider contextProvider;
    private final HubRoomProvider hubRoomProvider;
    private final HubClassProvider hubClassProvider;
    private final HubCourseProvider hubCourseProvider;
    private final SubmissionWindowValidator submissionWindowValidator;

    @Value("${checklist.timezone:America/Sao_Paulo}")
    private String timezone;

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

        if (!hubRoomProvider.existsById(request.roomId())) {
            throw new EntityNotFoundException("Sala nao encontrada no Hub.");
        }

        ClassReference classReference = hubClassProvider.findById(request.classId())
                .orElseThrow(() -> new EntityNotFoundException("Turma nao encontrada no Hub."));
        requireShiftPresent(classReference);

        validateCourseFromClass(classReference);

        RequestContext currentUser = contextProvider.getRequestContext();

        if (!currentUser.canOperateChecklistExecutionForClass(request.classId())) {
            throw new AccessDeniedException("Usuario nao tem permissao para criar checklist para a turma informada.");
        }

        Shift shift   = classReference.getShift();
        Period period = PeriodResolver.resolve(shift, request.checklistType());

        submissionWindowValidator.validate(request.classId(), request.checklistType());

        var now        = LocalDateTime.now(ZoneId.of(timezone));
        var startOfDay = now.toLocalDate().atStartOfDay();
        var endOfDay   = startOfDay.plusDays(1);

        boolean duplicated = repository.existsDuplicateChecklist(
                request.classId(),
                request.roomId(),
                period.name(),
                request.checklistType().name(),
                startOfDay,
                endOfDay
        );

        if (duplicated) {
            throw new IllegalArgumentException("Ja existe checklist para esta turma, sala, periodo, dia e tipo.");
        }

        ChecklistExecution execution = executionMapper.toDraftEntity(request, template, currentUser.userId(), now, shift, period);

        return repository.save(execution);
    }

    private void requireShiftPresent(ClassReference classReference) {
        if (classReference.getShift() == null) {
            throw new IllegalStateException("Turno da turma nao informado pelo Hub.");
        }
    }

    private void validateCourseFromClass(ClassReference classReference) {
        CourseReference courseRef = classReference.getCourseReference();
        if (courseRef == null) {
            return;
        }
        if (!hubCourseProvider.existsById(courseRef.getCourseId())) {
            throw new EntityNotFoundException("Curso da turma nao encontrado no Hub.");
        }
    }
}
