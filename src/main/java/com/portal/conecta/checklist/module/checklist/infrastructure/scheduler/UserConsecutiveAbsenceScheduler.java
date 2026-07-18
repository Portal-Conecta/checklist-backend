package com.portal.conecta.checklist.module.checklist.infrastructure.scheduler;

import com.portal.conecta.checklist.module.checklist.application.port.out.messaging.NotificationEventPublisher;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.shared.messaging.event.NotificationEvent;
import com.portal.conecta.checklist.shared.messaging.notification.NotificationEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "app.rabbitmq", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class UserConsecutiveAbsenceScheduler {

    private final ChecklistExecutionRepository executionRepository;
    private final NotificationEventPublisher eventPublisher;

    @Value("${checklist.timezone:America/Sao_Paulo}")
    private String timezone;

    @Scheduled(cron = "0 0 7 * * *", zone = "${checklist.timezone:America/Sao_Paulo}")
    public void checkThreeDaysConsecutiveAbsences() {
        log.info("Iniciando verificação de ausência de checklist por 3 dias consecutivos...");
        List<UUID> delinquentUserIds = executionRepository.findUsersWithThreeConsecutiveDaysWithoutSubmission();

        for (UUID userId : delinquentUserIds) {
            try {
                publishThreeDaysAbsenceNotification(userId);
            } catch (Exception e) {
                log.error("Falha ao publicar notificação de 3 dias sem checklist para o usuário {}: {}",
                        userId, e.getMessage(), e);
            }
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
                NotificationEventType.CHECKLIST_THREE_DAYS_MISSING,
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
