package com.portal.conecta.checklist.modules.checklist.infrastructure.scheduler;

import com.portal.conecta.checklist.modules.checklist.application.port.out.messaging.NotificationEventPublisher;
import com.portal.conecta.checklist.modules.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.shared.messaging.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserConsecutiveAbsenceScheduler {

    private final ChecklistExecutionRepository executionRepository;
    private final NotificationEventPublisher eventPublisher;

    @Value("${checklist.timezone:America/Sao_Paulo}")
    private String timezone;

    @Scheduled(cron = "0 0 7 * * *")
    public void checkThreeDaysConsecutiveAbsences() {
        log.info("Iniciando verficação de ausencia de checklist por 3 dias consegutivos...");
        List<UUID> delinquentUserIds = executionRepository.findUsersWithThreeConsecutiveDaysWithoutSubmission();

        for (UUID userId : delinquentUserIds) {
            publishThreeDaysAbsenceNotification(userId);
        }
    }

    private void publishThreeDaysAbsenceNotification(UUID userId) {
        var filters = List.<NotificationEvent.NotificationFilter>of();
        var scope = List.of(new NotificationEvent.NotificationScope("USER", userId.toString()));

        Map<String, Object> metadata = Map.of(
                "delinquentUserId", userId.toString(),
                "route", "/auditoria/usuarios/" + userId
        );

        NotificationEvent event = new NotificationEvent(
                "user-absence-3days-" + userId + "-" + LocalDate.now(ZoneId.of(timezone)),
                userId.toString(),
                "checklist-service",
                "checklist.three_days_missing",
                Instant.now(),
                "Alerta: Ausência de Checklist",
                "O usuário responsável está há 3 dias consecutivos sem realizar nenhuma submissão de checklist.",
                filters,
                scope,
                metadata
        );

        eventPublisher.publish(event);
        log.info("Notificação de 3 dias sem checklist enviada ao usuário: {}", userId);
    }
}
