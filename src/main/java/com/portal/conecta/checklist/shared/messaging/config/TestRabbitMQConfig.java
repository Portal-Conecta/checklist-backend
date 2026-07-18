package com.portal.conecta.checklist.shared.messaging.config;

import com.portal.conecta.checklist.module.checklist.application.port.out.messaging.NotificationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestRabbitMQConfig {


    @Bean
    @Primary
    public NotificationEventPublisher notificationEventPublisher() {
        return event -> {
            // classe para teste apenas
        };
    }
}