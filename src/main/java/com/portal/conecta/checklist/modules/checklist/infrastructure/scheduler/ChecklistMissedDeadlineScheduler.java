package com.portal.conecta.checklist.modules.checklist.infrastructure.scheduler;


import com.portal.conecta.checklist.modules.checklist.application.port.out.messaging.NotificationEventPublisher;
import com.portal.conecta.checklist.modules.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.shared.messaging.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; // <-- USE ESTE
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChecklistMissedDeadlineScheduler {

    private final ChecklistExecutionRepository executionRepository;
    private final NotificationEventPublisher notificationEventPublisher;


    @Value("${checklist.timezone:America/Sao_Paulo}")
    private String timezone;


    @Scheduled(cron = "0 0 22 * * *")
    public void checkPendingChecklists() {
        log.info("Verificando turmas que não realizaram o checklist hoje...");

        var now = LocalDate.now(ZoneId.of(timezone));
        LocalDateTime startOfDay = now.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<UUID> missingClassIds = executionRepository.findClassIdsWithMissedChecklist(startOfDay, endOfDay);

        for (UUID classId : missingClassIds) {
            publishMissedNotification(classId);
        }

    }
    private void publishMissedNotification(UUID classId) {
        var filters = List.of(new NotificationEvent.NotificationFilter("ROLE", "REPRESENTATIVE"));
        var scope = List.of(new NotificationEvent.NotificationScope("CLASS", classId.toString()));

        Map<String, Object> metadata = Map.of(
                "classId", classId.toString(),
                "route", "/turmas/" + classId + "/checklists/novo"
        );

        NotificationEvent event = new NotificationEvent(
                UUID.randomUUID().toString(),
                classId.toString(),
                "checklist-service",
                "checklist.missed_deadline",
                Instant.now(),
                "Checklist Não Realizado!",
                "O prazo de hoje expirou e o checklist da sua turma não foi preenchido.",
                filters,
                scope,
                metadata
        );

        notificationEventPublisher.publish(event);
        log.info("Notificação de checklist pendente enviada para a turma: {}", classId);
    }


}
