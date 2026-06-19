package com.portal.conecta.checklist.shared.messaging.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbitmq")
@Getter
@Setter
public class RabbitMQProperties {

    private Boolean enabled = true;
    private String exchange;
    private String queue;
    private String dlq;
    private String routingKey;
}
