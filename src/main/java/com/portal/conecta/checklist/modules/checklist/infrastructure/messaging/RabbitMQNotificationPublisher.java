package com.portal.conecta.checklist.modules.checklist.infrastructure.messaging;

import com.portal.conecta.checklist.shared.messaging.event.NotificationEvent;
import com.portal.conecta.checklist.modules.checklist.application.port.out.messaging.NotificationEventPublisher;
import com.portal.conecta.checklist.shared.messaging.config.RabbitMQProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")  // ← ADICIONAR ISSO
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