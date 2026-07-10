package com.portal.conecta.checklist.modules.checklist.application.service.window;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.modules.checklist.domain.exception.SubmissionWindowViolationException;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistSubmissionWindow;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistSubmissionWindowRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Valida se o instante atual esta dentro da janela de envio configurada para a turma e tipo de checklist.
 * Quando nenhuma janela esta configurada para a combinacao, a operacao e permitida sem restricao.
 */
@Component
@RequiredArgsConstructor
public class SubmissionWindowValidator {

    private final ChecklistSubmissionWindowRepositoryPort windowRepository;
    private final Clock clock;

    public void validate(UUID classId, ChecklistType checklistType) {
        windowRepository.findByClassIdAndChecklistType(classId, checklistType)
                .ifPresent(this::validateWindow);
    }

    private void validateWindow(ChecklistSubmissionWindow window) {
        LocalTime now = LocalTime.now(clock);
        LocalTime openAt = window.getOpenAt();
        LocalTime closeAt = openAt.plusMinutes(window.getDurationMinutes());

        if (now.isBefore(openAt) || now.isAfter(closeAt)) {
            throw new SubmissionWindowViolationException(openAt, closeAt);
        }
    }
}
