package com.portal.conecta.checklist.modules.checklist.application.service.window;

import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistSubmissionWindowRepositoryPort;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.modules.checklist.domain.exception.SubmissionWindowViolationException;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistSubmissionWindow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubmissionWindowValidatorTest {

    private static final ZoneId TIMEZONE = ZoneId.of("America/Sao_Paulo");
    private static final UUID CLASS_ID = UUID.fromString("22222222-2222-2222-2222-222222222221");

    // Relógio fixo em 14:00 BRT (17:00 UTC) — longe da meia-noite, determinístico
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-01-01T17:00:00Z"),
            TIMEZONE
    );

    private final ChecklistSubmissionWindowRepositoryPort repository =
            mock(ChecklistSubmissionWindowRepositoryPort.class);

    private final SubmissionWindowValidator validator =
            new SubmissionWindowValidator(repository, FIXED_CLOCK);

    @Test
    @DisplayName("deve permitir envio quando nao existe janela configurada")
    void devePermitirEnvioQuandoNaoExisteJanelaConfigurada() {
        when(repository.findByClassIdAndChecklistType(CLASS_ID, ChecklistType.ARRIVAL))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> validator.validate(CLASS_ID, ChecklistType.ARRIVAL));
    }

    @Test
    @DisplayName("deve permitir envio dentro da janela configurada")
    void devePermitirEnvioDentroDaJanelaConfigurada() {
        // clock fixo em 14:00 BRT → janela 13:55 até 14:25 → dentro
        LocalTime openAt = LocalTime.of(13, 55);
        ChecklistSubmissionWindow window = window(openAt, 30);

        when(repository.findByClassIdAndChecklistType(CLASS_ID, ChecklistType.ARRIVAL))
                .thenReturn(Optional.of(window));

        assertDoesNotThrow(() -> validator.validate(CLASS_ID, ChecklistType.ARRIVAL));
    }

    @Test
    @DisplayName("deve rejeitar envio antes da janela configurada")
    void deveRejeitarEnvioAntesDaJanelaConfigurada() {
        // clock fixo em 14:00 BRT → janela 14:05 até 14:35 → antes
        LocalTime openAt = LocalTime.of(14, 5);
        ChecklistSubmissionWindow window = window(openAt, 30);

        when(repository.findByClassIdAndChecklistType(CLASS_ID, ChecklistType.ARRIVAL))
                .thenReturn(Optional.of(window));

        assertThrows(SubmissionWindowViolationException.class,
                () -> validator.validate(CLASS_ID, ChecklistType.ARRIVAL));
    }

    @Test
    @DisplayName("deve rejeitar envio depois da janela configurada")
    void deveRejeitarEnvioDepoisDaJanelaConfigurada() {
        // clock fixo em 14:00 BRT → janela 13:00 até 13:30 → depois
        LocalTime openAt = LocalTime.of(13, 0);
        ChecklistSubmissionWindow window = window(openAt, 30);

        when(repository.findByClassIdAndChecklistType(CLASS_ID, ChecklistType.ARRIVAL))
                .thenReturn(Optional.of(window));

        assertThrows(SubmissionWindowViolationException.class,
                () -> validator.validate(CLASS_ID, ChecklistType.ARRIVAL));
    }

    private ChecklistSubmissionWindow window(LocalTime openAt, int durationMinutes) {
        return ChecklistSubmissionWindow.builder()
                .classId(CLASS_ID)
                .checklistType(ChecklistType.ARRIVAL)
                .openAt(openAt)
                .durationMinutes(durationMinutes)
                .build();
    }
}
