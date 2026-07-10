package com.portal.conecta.checklist.integration.shared.messaging;

import com.portal.conecta.checklist.modules.checklist.application.port.out.messaging.NotificationEventPublisher;
import com.portal.conecta.checklist.shared.messaging.config.RabbitMQProperties;
import com.portal.conecta.checklist.shared.messaging.event.NotificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "checklist.security.jwt.secret=dGVzdC1vbmx5LWp3dC1zZWNyZXQtMzItYnl0ZXMtbm90LXJlYWw=",
                "checklist.security.swagger-public=true",
                "hub.api.url=http://localhost:8080",
                "spring.docker.compose.enabled=false",
                "app.rabbitmq.enabled=true",
                "app.rabbitmq.exchange=notifications.exchange",
                "app.rabbitmq.queue=notifications.dispatch.q",
                "app.rabbitmq.dlq=notifications.dispatch.dlq",
                "app.rabbitmq.routing-key=notification.requested"
        }
)
@ActiveProfiles("integration")
class RabbitMQPhysicalQueueIntegrationTest {

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.13-management");

    @DynamicPropertySource
    static void overrideRabbitConnectionProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitMQContainer::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMQContainer::getAdminPassword);
    }

    @Autowired
    private NotificationEventPublisher publisher;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private RabbitMQProperties props;

    @Autowired
    @Qualifier("notificationsQueue")
    private Queue notificationsQueue;

    private NotificationEvent event;

    @BeforeEach
    void setUp() {
        rabbitAdmin.purgeQueue(props.getQueue(), false);
        rabbitAdmin.purgeQueue(props.getDlq(), false);

        event = new NotificationEvent(
                "msg-physical-1",
                "corr-physical-1",
                "checklist-api",
                "checklist.non_compliance.created",
                Instant.parse("2026-06-26T12:00:00Z"),
                "Nao conformidade identificada",
                "Um checklist foi submetido com itens nao conformes.",
                List.of(new NotificationEvent.NotificationFilter("ROLE", "TEACHER")),
                List.of(new NotificationEvent.NotificationScope("CLASS", "8f8e8d8c-8b8a-8f8e-8d8c-8b8a8f8e8d8c")),
                Map.of("executionId", "execution-physical-1")
        );
    }

    @Test
    void shouldDeliverPublishedMessageToNotificationsQueue() {
        publisher.publish(event);

        Message received = rabbitTemplate.receive(props.getQueue(), 5_000);

        assertThat(received).isNotNull();
    }

    @Test
    void shouldSerializeEventBodyAsJson() {
        publisher.publish(event);

        Message received = rabbitTemplate.receive(props.getQueue(), 5_000);

        assertThat(received).isNotNull();
        assertThat(received.getMessageProperties().getContentType()).isEqualTo("application/json");

        String body = new String(received.getBody());
        assertThat(body)
                .contains("\"messageId\":\"msg-physical-1\"")
                .contains("\"correlationId\":\"corr-physical-1\"")
                .contains("\"source\":\"checklist-api\"")
                .contains("\"eventType\":\"checklist.non_compliance.created\"")
                .contains("\"occurredAt\":")
                .contains("\"title\":")
                .contains("\"body\":")
                .contains("\"scope\":")
                .contains("\"filters\":");
    }

    @Test
    void shouldRouteMessageToQueueWithExpectedRoutingKey() {
        publisher.publish(event);

        Message received = rabbitTemplate.receive(props.getQueue(), 5_000);

        assertThat(received).isNotNull();
        assertThat(received.getMessageProperties().getReceivedRoutingKey())
                .isEqualTo("notification.requested");
    }

    @Test
    void shouldDeclareNotificationsQueueWithDeadLetterArguments() {
        assertThat(notificationsQueue.getArguments())
                .containsEntry("x-dead-letter-exchange", "")
                .containsEntry("x-dead-letter-routing-key", "notifications.dispatch.dlq");
    }

    @Test
    void shouldRouteNackedMessageToDeadLetterQueue() {
        publisher.publish(event);

        rabbitTemplate.execute(channel -> {
            com.rabbitmq.client.GetResponse response = channel.basicGet(props.getQueue(), false);
            assertThat(response).isNotNull();
            channel.basicNack(response.getEnvelope().getDeliveryTag(), false, false);
            return null;
        });

        Message dlqMessage = rabbitTemplate.receive(props.getDlq(), 5_000);

        assertThat(dlqMessage).isNotNull();
        assertThat(new String(dlqMessage.getBody()))
                .contains("\"messageId\":\"msg-physical-1\"");
    }
}
