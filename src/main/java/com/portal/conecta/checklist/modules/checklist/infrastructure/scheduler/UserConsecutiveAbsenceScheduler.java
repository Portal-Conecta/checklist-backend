package com.portal.conecta.checklist.modules.checklist.infrastructure.scheduler;

import com.portal.conecta.checklist.modules.checklist.application.port.out.messaging.NotificationEventPublisher;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.modules.checklist.infrastructure.persistence.ChecklistExecutionRepository;
import com.portal.conecta.checklist.shared.messaging.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserConsecutiveAbsenceScheduler {

    private final ChecklistExecutionRepository executionRepository;
    private final NotificationEventPublisher eventPublisher;



    @Scheduled(cron = "0 0 7 * * *")
    public void checkThreeDaysConsecutiveAbsences(){
        log.info("Iniciando verficação de ausencia de checklist por 3 dias consegutivos...");
        List<UUID> delinquentUserIds = executionRepository.hasThreeConsecutiveDaysWithoutSubmission();


        for (UUID userIds: delinquentUserIds){
            publishThreeDaysAbsenceNotification(userIds);
        }
    }

    private void publishThreeDaysAbsenceNotification(UUID userId){
        var filters = List.of(new NotificationEvent.NotificationFilter("ROLE", "WEG"));
        var scope = List.of(new NotificationEvent.NotificationScope("USER", userId.toString()));

        Map<String, Object> metadata = Map.of(
                "delinquentUserId", userId.toString(),
                "route", "/auditoria/usuarios/" + userId
        );

        NotificationEvent event = new NotificationEvent(
                UUID.randomUUID().toString(),
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
        log.info("Notificação de 3 dias sem checklist enviada para ROLE WEG a respeito do usuário: {}", userId);
    }



}
