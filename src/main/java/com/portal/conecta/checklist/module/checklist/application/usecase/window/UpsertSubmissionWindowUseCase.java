package com.portal.conecta.checklist.module.checklist.application.usecase.window;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.module.checklist.domain.enums.Shift;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistSubmissionWindow;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistSubmissionWindowRepository;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.SubmissionWindowRequestDTO;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class UpsertSubmissionWindowUseCase {

    private final ChecklistSubmissionWindowRepository repository;
    private final RequestContextProvider contextProvider;

    @Transactional
    public ChecklistSubmissionWindow execute(Shift shift, ChecklistType checklistType, SubmissionWindowRequestDTO request) {
        if (!contextProvider.getRequestContext().canManageChecklistTemplates()) {
            throw new AccessDeniedException("Apenas SENAI e WEG podem configurar janelas de envio.");
        }

        validateNoMidnightCrossing(request.openAt(), request.durationMinutes());

        ChecklistSubmissionWindow window = repository
                .findByShiftAndChecklistType(shift, checklistType)
                .orElseGet(() -> ChecklistSubmissionWindow.builder()
                        .shift(shift)
                        .checklistType(checklistType)
                        .build());

        window.setOpenAt(request.openAt());
        window.setDurationMinutes(request.durationMinutes());

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
}
