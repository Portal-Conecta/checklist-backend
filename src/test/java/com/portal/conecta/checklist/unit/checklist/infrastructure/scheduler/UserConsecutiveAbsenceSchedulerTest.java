package com.portal.conecta.checklist.unit.checklist.infrastructure.scheduler;

import com.portal.conecta.checklist.modules.checklist.application.port.out.messaging.NotificationEventPublisher;
import com.portal.conecta.checklist.modules.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.modules.checklist.infrastructure.scheduler.UserConsecutiveAbsenceScheduler;
import com.portal.conecta.checklist.shared.messaging.event.NotificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserConsecutiveAbsenceSchedulerTest {

    @Mock
    private ChecklistExecutionRepository executionRepository;

    @Mock
    private NotificationEventPublisher notificationPublisher;

    @InjectMocks
    private UserConsecutiveAbsenceScheduler scheduler;

    @Captor
    private ArgumentCaptor<NotificationEvent> eventCaptor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduler, "timezone", "America/Sao_Paulo");
    }

    @Test
    @DisplayName("deve ser um componente Spring")
    void deveSerComponenteSpring() {
        assertTrue(UserConsecutiveAbsenceScheduler.class.isAnnotationPresent(Component.class));
    }

    @Test
    @DisplayName("metodo deve ter anotacao @Scheduled com cron correto para as 7h da manha")
    void deveTerAnotacaoScheduled() throws NoSuchMethodException {
        var method = UserConsecutiveAbsenceScheduler.class.getMethod("checkThreeDaysConsecutiveAbsences");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);

        assertNotNull(scheduled);
        assertEquals("0 0 7 * * *", scheduled.cron());
    }

    @DisplayName("Should publish absence event with correct messageId and empty filters")
    @Test
    void devePublicarEventoParaRoleWeg() {
        UUID userId = UUID.randomUUID();

        when(executionRepository.findUsersWithThreeConsecutiveDaysWithoutSubmission())
                .thenReturn(List.of(userId));

        scheduler.checkThreeDaysConsecutiveAbsences();

        verify(notificationPublisher, times(1)).publish(eventCaptor.capture());

        NotificationEvent event = eventCaptor.getValue();

        assertTrue(event.filters().isEmpty());
        assertTrue(event.messageId().startsWith("user-absence-3days-"));

        // Verifica se o escopo aponta pro usuario correto
        assertTrue(event.scope().stream().anyMatch(s -> s.type().equals("USER") && s.correlationId().equals(userId.toString())));
    }
}