package com.portal.conecta.checklist.shared.messaging.config.test;

import com.portal.conecta.checklist.modules.checklist.application.port.out.messaging.NotificationEventPublisher;
import com.portal.conecta.checklist.shared.messaging.config.RabbitMQProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestRabbitMQConfiguration {

    @Bean
    @Primary
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate();
    }

    @Bean
    @Primary
    public RabbitMQProperties rabbitMQProperties() {
        RabbitMQProperties props = new RabbitMQProperties();
        props.setExchange("test.exchange");
        props.setQueue("test.queue");
        props.setDlq("test.dlq");
        props.setRoutingKey("test.routing");
        return props;
    }

    @Bean
    @Primary
    public NotificationEventPublisher notificationEventPublisher() {
        return event -> {
            // classe para teste apenas
        };
    }
}