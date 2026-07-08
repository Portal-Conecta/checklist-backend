package com.portal.conecta.checklist.modules.checklist.infrastructure.messaging;

import com.portal.conecta.checklist.modules.checklist.application.port.out.messaging.NotificationEventPublisher;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.submit.ChecklistNonComplianceEvent;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.shared.messaging.event.NotificationEvent;
import com.portal.conecta.checklist.shared.messaging.notification.NotificationEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChecklistNonComplianceEventListener {

    private final NotificationEventPublisher notificationPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChecklistNonComplianceEvent event) {
        ChecklistExecution execution = event.execution();

        NotificationEvent notificationEvent = new NotificationEvent(
                "checklist-noncompliance-" + execution.getId(),
                execution.getId().toString(),
                "checklist-service",
                NotificationEventType.CHECKLIST_NON_COMPLIANCE_CREATED,
                Instant.now(),
                "Não Conformidade Identificada",
                "Um checklist foi submetido com itens não conformes na turma. Pontuação: " + execution.getComplianceScore() + "%.",
                List.of(new NotificationEvent.NotificationFilter("ROLE", "TEACHER")),
                List.of(new NotificationEvent.NotificationScope("CLASS", execution.getClassId().toString())),
                Map.of(
                        "executionId", execution.getId().toString(),
                        "classId", execution.getClassId().toString(),
                        "score", execution.getComplianceScore(),
                        "route", "/turmas/" + execution.getClassId() + "/checklists/" + execution.getId()
                )
        );

        notificationPublisher.publish(notificationEvent);
    }
}
