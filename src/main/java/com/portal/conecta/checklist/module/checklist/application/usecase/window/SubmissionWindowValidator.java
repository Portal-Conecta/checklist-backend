package com.portal.conecta.checklist.module.checklist.application.usecase.window;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.module.checklist.domain.enums.Shift;
import com.portal.conecta.checklist.module.checklist.domain.exception.SubmissionWindowViolationException;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistSubmissionWindow;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistSubmissionWindowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Valida se o instante atual esta dentro da janela de envio configurada para a turma e tipo de checklist.
 * Quando nenhuma janela esta configurada para a combinacao, a operacao e permitida sem restricao.
 */
@Component
@RequiredArgsConstructor
public class SubmissionWindowValidator {

    private final ChecklistSubmissionWindowRepository windowRepository;

    @Value("${checklist.timezone:America/Sao_Paulo}")
    private String timezone;

    public void validate(UUID classId, ChecklistType checklistType) {
        windowRepository.findByClassIdAndChecklistType(classId, checklistType)
                .ifPresent(this::validateWindow);
    }

    public void validate(Shift shift, ChecklistType checklistType) {
        windowRepository.findByShiftAndChecklistType(shift, checklistType)
                .ifPresent(this::validateWindow);
    }

    private void validateWindow(ChecklistSubmissionWindow window) {
        LocalTime now = LocalTime.now(ZoneId.of(timezone));
        LocalTime openAt  = window.getOpenAt();
        LocalTime closeAt = openAt.plusMinutes(window.getDurationMinutes());

        if (now.isBefore(openAt) || now.isAfter(closeAt)) {
            throw new SubmissionWindowViolationException(openAt, closeAt);
        }
    }
}
