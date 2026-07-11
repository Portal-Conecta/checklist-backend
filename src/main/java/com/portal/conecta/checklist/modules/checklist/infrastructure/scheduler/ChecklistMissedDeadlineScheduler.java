package com.portal.conecta.checklist.modules.checklist.infrastructure.scheduler;

import com.portal.conecta.checklist.modules.checklist.application.port.out.messaging.NotificationEventPublisher;
import com.portal.conecta.checklist.modules.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.modules.checklist.infrastructure.persistence.MissedChecklistSummary;
import com.portal.conecta.checklist.shared.messaging.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Scheduled(cron = "0 0 22 * * *", zone = "${checklist.timezone:America/Sao_Paulo}")
    public void checkPendingChecklists() {
        log.info("Verificando turmas que não realizaram o checklist hoje...");

        var now = LocalDate.now(ZoneId.of(timezone));
        LocalDateTime startOfDay = now.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<MissedChecklistSummary> missingClasses = executionRepository.findClassIdsWithMissedChecklist(startOfDay, endOfDay);

        for (MissedChecklistSummary summary : missingClasses) {
            try {
                publishMissedNotification(summary.getClassId(), summary.getChecklistType());
            } catch (Exception e) {
                log.error("Falha ao publicar notificação de checklist perdido para a turma {}: {}",
                        summary.getClassId(), e.getMessage(), e);
            }
        }
    }

    private void publishMissedNotification(UUID classId, String checklistType) {
        var filters = List.of(new NotificationEvent.NotificationFilter("ROLE", "REPRESENTATIVE"));
        var scope = List.of(new NotificationEvent.NotificationScope("CLASS", classId.toString()));

        Map<String, Object> metadata = Map.of(
                "classId", classId.toString(),
                "checklistType", checklistType,
                "route", "/turmas/" + classId + "/checklists/novo"
        );

        NotificationEvent event = new NotificationEvent(
                "checklist-missed-" + classId + "-" + checklistType + "-" + LocalDate.now(ZoneId.of(timezone)),
                classId.toString(),
                "checklist-service",
                "checklist.missed_deadline",
                Instant.now(),
                "Checklist Não Realizado!",
                "O prazo de hoje expirou e o checklist " + checklistType + " da sua turma não foi preenchido.",
                filters,
                scope,
                metadata
        );

        notificationEventPublisher.publish(event);
        log.info("Notificação de checklist pendente enviada para a turma: {}", classId);
    }
}
