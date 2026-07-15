package com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.create;

import com.portal.conecta.checklist.modules.checklist.application.service.execution.ChecklistExecutionDataMapper;
import com.portal.conecta.checklist.modules.checklist.application.service.window.SubmissionWindowValidator;
import com.portal.conecta.checklist.modules.checklist.domain.service.PeriodResolver;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.modules.checklist.domain.enums.Period;
import com.portal.conecta.checklist.modules.checklist.domain.enums.Shift;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.domain.valueobject.ClassReference;
import com.portal.conecta.checklist.modules.checklist.domain.valueobject.CourseReference;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistExecutionRepositoryPort;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistTemplateRepositoryPort;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubClassProvider;
import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubCourseProvider;
import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubRoomProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateChecklistExecutionUseCase {

    private final ChecklistExecutionRepositoryPort repository;
    private final ChecklistTemplateRepositoryPort templateRepository;
    private final ChecklistExecutionDataMapper executionMapper;
    private final RequestContextProvider contextProvider;
    private final HubRoomProvider hubRoomProvider;
    private final HubClassProvider hubClassProvider;
    private final HubCourseProvider hubCourseProvider;
    private final SubmissionWindowValidator submissionWindowValidator;

    @Value("${checklist.timezone:America/Sao_Paulo}")
    private String timezone;

    @Transactional
    public ChecklistExecution execute(CreateChecklistExecutionCommand command) {
        log.info("Criando rascunho de checklist templateId={} classId={}",
                command.templateId(), command.classId());

        ChecklistTemplate template = templateRepository.findById(command.templateId())
                .orElseThrow(() -> new EntityNotFoundException("Template nao encontrado."));

        if (!template.isActive() || template.getStatus() != ChecklistTemplateStatus.ACTIVE) {
            throw new IllegalStateException("Template nao esta ativo.");
        }

        if (!hubRoomProvider.existsById(template.getRoomId())) {
            throw new EntityNotFoundException("Sala nao encontrada no Hub.");
        }

        ClassReference classReference = hubClassProvider.findById(command.classId())
                .orElseThrow(() -> new EntityNotFoundException("Turma nao encontrada no Hub."));
        requireShiftPresent(classReference);

        validateCourseFromClass(classReference);

        RequestContext currentUser = contextProvider.getRequestContext();

        if (!currentUser.canOperateChecklistExecutionForClass(command.classId())) {
            throw new AccessDeniedException("Usuario nao tem permissao para criar checklist para a turma informada.");
        }

        Shift shift   = classReference.getShift();
        Period period = PeriodResolver.resolve(shift, command.checklistType());

        submissionWindowValidator.validate(command.classId(), command.checklistType());

        var now        = LocalDateTime.now(ZoneId.of(timezone));
        var startOfDay = now.toLocalDate().atStartOfDay();
        var endOfDay   = startOfDay.plusDays(1);

        boolean duplicated = repository.existsDuplicateChecklist(
                command.classId(),
                template.getRoomId(),
                period.name(),
                command.checklistType().name(),
                startOfDay,
                endOfDay
        );

        if (duplicated) {
            throw new IllegalArgumentException("Ja existe checklist para esta turma, sala, periodo, dia e tipo.");
        }

        ChecklistExecution execution = executionMapper.toDraftEntity(command, template, currentUser.userId(), now, shift, period);

        ChecklistExecution created = repository.save(execution);
        log.info("Rascunho de checklist criado com sucesso executionId={} classId={}",
                created.getId(), created.getClassId());

        return created;
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
