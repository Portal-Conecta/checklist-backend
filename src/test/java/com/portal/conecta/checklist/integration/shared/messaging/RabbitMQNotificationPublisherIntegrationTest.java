package com.portal.conecta.checklist.integration.shared.messaging;

import com.portal.conecta.checklist.modules.checklist.application.port.out.messaging.NotificationEventPublisher;
import com.portal.conecta.checklist.modules.checklist.infrastructure.messaging.RabbitMQNotificationPublisher;
import com.portal.conecta.checklist.shared.messaging.config.RabbitMQProperties;
import com.portal.conecta.checklist.shared.messaging.event.NotificationEvent;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RabbitMQNotificationPublisherIntegrationTest {

    @Test
    void shouldPublishNotificationEventUsingSpringConfiguredProperties() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);

        new ApplicationContextRunner()
                .withBean(RabbitTemplate.class, () -> rabbitTemplate)
                .withUserConfiguration(PublisherTestConfig.class, RabbitMQNotificationPublisher.class)
                .withPropertyValues(
                        "app.rabbitmq.enabled=true",
                        "app.rabbitmq.exchange=notifications.exchange",
                        "app.rabbitmq.routing-key=notification.requested"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(NotificationEventPublisher.class);

                    NotificationEvent event = notificationEvent();
                    context.getBean(NotificationEventPublisher.class).publish(event);

                    verify(rabbitTemplate).convertAndSend(
                            "notifications.exchange",
                            "notification.requested",
                            event
                    );
                });
    }

    @Test
    void shouldNotCreatePublisherWhenMessagingIsDisabled() {
        new ApplicationContextRunner()
                .withBean(RabbitTemplate.class, () -> mock(RabbitTemplate.class))
                .withUserConfiguration(PublisherTestConfig.class, RabbitMQNotificationPublisher.class)
                .withPropertyValues("app.rabbitmq.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(NotificationEventPublisher.class));
    }

    private NotificationEvent notificationEvent() {
        return new NotificationEvent(
                "message-1",
                "correlation-1",
                "checklist-service",
                "checklist.non_compliance.created",
                Instant.parse("2026-06-18T12:00:00Z"),
                "Nao conformidade identificada",
                "Um checklist foi submetido com itens nao conformes.",
                List.of(new NotificationEvent.NotificationFilter("ROLE", "WEG")),
                List.of(new NotificationEvent.NotificationScope("CLASS", "8f8e8d8c-8b8a-8f8e-8d8c-8b8a8f8e8d8c")),
                Map.of("executionId", "execution-1")
        );
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(RabbitMQProperties.class)
    static class PublisherTestConfig {
    }
}
