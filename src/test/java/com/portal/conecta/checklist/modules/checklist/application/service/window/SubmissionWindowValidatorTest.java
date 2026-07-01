package com.portal.conecta.checklist.modules.checklist.application.service.window;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.modules.checklist.domain.exception.SubmissionWindowViolationException;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistSubmissionWindow;
import com.portal.conecta.checklist.modules.checklist.infrastructure.persistence.ChecklistSubmissionWindowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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

    private final ChecklistSubmissionWindowRepository repository = mock(ChecklistSubmissionWindowRepository.class);
    private final SubmissionWindowValidator validator = new SubmissionWindowValidator(repository);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(validator, "timezone", "America/Sao_Paulo");
    }

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
        LocalTime openAt = LocalTime.now(TIMEZONE).minusMinutes(5);
        ChecklistSubmissionWindow window = window(openAt, 30);

        when(repository.findByClassIdAndChecklistType(CLASS_ID, ChecklistType.ARRIVAL))
                .thenReturn(Optional.of(window));

        assertDoesNotThrow(() -> validator.validate(CLASS_ID, ChecklistType.ARRIVAL));
    }

    @Test
    @DisplayName("deve rejeitar envio antes da janela configurada")
    void deveRejeitarEnvioAntesDaJanelaConfigurada() {
        LocalTime openAt = LocalTime.now(TIMEZONE).plusMinutes(5);
        ChecklistSubmissionWindow window = window(openAt, 30);

        when(repository.findByClassIdAndChecklistType(CLASS_ID, ChecklistType.ARRIVAL))
                .thenReturn(Optional.of(window));

        assertThrows(SubmissionWindowViolationException.class,
                () -> validator.validate(CLASS_ID, ChecklistType.ARRIVAL));
    }

    @Test
    @DisplayName("deve rejeitar envio depois da janela configurada")
    void deveRejeitarEnvioDepoisDaJanelaConfigurada() {
        LocalTime openAt = LocalTime.now(TIMEZONE).minusMinutes(40);
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
