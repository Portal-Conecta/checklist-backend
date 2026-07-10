package com.portal.conecta.checklist.unit.checklist.infrastructure.messaging;

import com.portal.conecta.checklist.modules.checklist.infrastructure.messaging.RabbitMQNotificationPublisher;
import com.portal.conecta.checklist.shared.messaging.config.RabbitMQProperties;
import com.portal.conecta.checklist.shared.messaging.event.NotificationEvent;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RabbitMQNotificationPublisherTest {

    @Test
    void shouldPublishNotificationEventUsingConfiguredExchangeAndRoutingKey() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        RabbitMQProperties properties = new RabbitMQProperties();
        properties.setExchange("notifications.exchange");
        properties.setRoutingKey("notification.requested");

        RabbitMQNotificationPublisher publisher = new RabbitMQNotificationPublisher(rabbitTemplate, properties);
        NotificationEvent event = new NotificationEvent(
                "message-1",
                "correlation-1",
                "checklist-api",
                "CHECKLIST_SUBMITTED",
                Instant.parse("2026-06-18T12:00:00Z"),
                "Checklist enviado",
                "Um checklist foi enviado.",
                List.of(new NotificationEvent.NotificationFilter("ROLE", "TEACHER")),
                List.of(new NotificationEvent.NotificationScope("CLASS", "8f8e8d8c-8b8a-8f8e-8d8c-8b8a8f8e8d8c")),
                Map.of("executionId", "execution-1")
        );

        publisher.publish(event);

        verify(rabbitTemplate).convertAndSend(
                "notifications.exchange",
                "notification.requested",
                event
        );
    }
}
