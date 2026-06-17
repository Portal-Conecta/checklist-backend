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
    public NotificationEventPublisher notificationEventPublisher() {
        return event -> {
            // classe para teste apenas
        };
    }
}