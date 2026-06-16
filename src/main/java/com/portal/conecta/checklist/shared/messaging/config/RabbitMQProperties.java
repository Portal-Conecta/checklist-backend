package com.portal.conecta.checklist.shared.messaging.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "app.rabbitmq")
@Component
@Getter
@Setter
public class RabbitMQProperties {

    private String exchange;
    private String queue;
    private String dlq;
    private String routingKey;

}