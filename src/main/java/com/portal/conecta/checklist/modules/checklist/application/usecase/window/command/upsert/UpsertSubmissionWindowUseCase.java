package com.portal.conecta.checklist.modules.checklist.application.usecase.window.command.upsert;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistSubmissionWindow;
import com.portal.conecta.checklist.modules.checklist.domain.valueobject.ClassReference;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistSubmissionWindowRepositoryPort;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubClassProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpsertSubmissionWindowUseCase {

    private final ChecklistSubmissionWindowRepositoryPort repository;
    private final RequestContextProvider contextProvider;
    private final HubClassProvider hubClassProvider;

    @Transactional
    public ChecklistSubmissionWindow execute(UUID classId, ChecklistType checklistType, UpsertSubmissionWindowCommand command) {
        if (!contextProvider.getRequestContext().canManageChecklistTemplates()) {
            throw new AccessDeniedException("Apenas SENAI e WEG podem configurar janelas de envio.");
        }

        validateNoMidnightCrossing(command.openAt(), command.durationMinutes());

        ClassReference classReference = hubClassProvider.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException("Turma nao encontrada no Hub."));
        requireShiftPresent(classReference);

        ChecklistSubmissionWindow window = repository
                .findByClassIdAndChecklistType(classId, checklistType)
                .orElseGet(() -> ChecklistSubmissionWindow.builder()
                        .classId(classId)
                        .checklistType(checklistType)
                        .build());

        window.setShift(classReference.getShift());
        window.setOpenAt(command.openAt());
        window.setDurationMinutes(command.durationMinutes());

        return repository.save(window);
    }

    private void validateNoMidnightCrossing(LocalTime openAt, int durationMinutes) {
        LocalTime closeAt = openAt.plusMinutes(durationMinutes);
        if (!closeAt.isAfter(openAt)) {
            throw new IllegalArgumentException(
                    "Janela de envio nao pode ultrapassar meia-noite. " +
                    "Revise openAt e durationMinutes.");
        }
    }

    private void requireShiftPresent(ClassReference classReference) {
        if (classReference.getShift() == null) {
            throw new IllegalStateException("Turno da turma nao informado pelo Hub.");
        }
    }
}
