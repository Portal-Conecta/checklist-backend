package com.portal.conecta.checklist.module.checklist.infrastructure.messaging;

import com.portal.conecta.checklist.module.checklist.application.port.out.messaging.NotificationEventPublisher;
import com.portal.conecta.checklist.shared.messaging.config.RabbitMQProperties;
import com.portal.conecta.checklist.shared.messaging.event.NotificationEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.rabbitmq", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMQNotificationPublisher implements NotificationEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties props;

    public RabbitMQNotificationPublisher(RabbitTemplate rabbitTemplate, RabbitMQProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    @Override
    public void publish(NotificationEvent event) {
        rabbitTemplate.convertAndSend(
                props.getExchange(),
                props.getRoutingKey(),
                event
        );
    }
}
