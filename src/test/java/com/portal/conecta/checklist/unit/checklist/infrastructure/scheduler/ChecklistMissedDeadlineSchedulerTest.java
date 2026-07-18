package com.portal.conecta.checklist.unit.checklist.infrastructure.scheduler;

import com.portal.conecta.checklist.modules.checklist.application.port.out.messaging.NotificationEventPublisher;
import com.portal.conecta.checklist.modules.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.modules.checklist.infrastructure.persistence.MissedChecklistSummary;
import com.portal.conecta.checklist.modules.checklist.infrastructure.scheduler.ChecklistMissedDeadlineScheduler;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChecklistMissedDeadlineSchedulerTest {

    @Mock
    private ChecklistExecutionRepository executionRepository;

    @Mock
    private NotificationEventPublisher notificationPublisher;

    @InjectMocks
    private ChecklistMissedDeadlineScheduler scheduler;

    @Captor
    private ArgumentCaptor<NotificationEvent> eventCaptor;

    @BeforeEach
    void setUp() {
        // Simula a injeção do @Value do Spring
        ReflectionTestUtils.setField(scheduler, "timezone", "America/Sao_Paulo");
    }

    @Test
    @DisplayName("deve ser um componente Spring")
    void deveSerComponenteSpring() {
        assertTrue(ChecklistMissedDeadlineScheduler.class.isAnnotationPresent(Component.class));
    }

    @Test
    @DisplayName("metodo deve ter anotacao @Scheduled com cron correto")
    void deveTerAnotacaoScheduled() throws NoSuchMethodException {
        var method = ChecklistMissedDeadlineScheduler.class.getMethod("checkPendingChecklists");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);

        assertNotNull(scheduled);
        assertEquals("0 0 22 * * *", scheduled.cron());
    }

    @Test
    @DisplayName("deve publicar evento de notificacao para cada turma pendente")
    void devePublicarEventoParaTurmasPendentes() {
        UUID classId1 = UUID.randomUUID();
        UUID classId2 = UUID.randomUUID();

        MissedChecklistSummary summary1 = missedChecklistSummary(classId1, "ARRIVAL");
        MissedChecklistSummary summary2 = missedChecklistSummary(classId2, "ARRIVAL");

        when(executionRepository.findClassIdsWithMissedChecklist(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(summary1, summary2));

        scheduler.checkPendingChecklists();

        verify(notificationPublisher, times(2)).publish(eventCaptor.capture());

        List<NotificationEvent> publishedEvents = eventCaptor.getAllValues();

        // Verifica o primeiro evento disparado
        NotificationEvent firstEvent = publishedEvents.get(0);
        assertEquals("checklist-service", firstEvent.source());
        assertEquals("checklist.missed_deadline", firstEvent.eventType());
        assertEquals(classId1.toString(), firstEvent.correlationId());

        // Verifica se o contrato de filters/scope está respeitado
        assertTrue(firstEvent.filters().stream().anyMatch(f -> f.type().equals("ROLE") && f.value().equals("REPRESENTATIVE")));
        assertTrue(firstEvent.scope().stream().anyMatch(s -> s.type().equals("CLASS") && s.correlationId().equals(classId1.toString())));
    }

    private MissedChecklistSummary missedChecklistSummary(UUID classId, String checklistType) {
        return new MissedChecklistSummary() {
            @Override
            public UUID getClassId() {
                return classId;
            }

            @Override
            public String getChecklistType() {
                return checklistType;
            }
        };
    }
}