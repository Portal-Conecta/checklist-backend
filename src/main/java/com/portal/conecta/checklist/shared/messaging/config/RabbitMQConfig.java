package com.portal.conecta.checklist.shared.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableRabbit
@EnableConfigurationProperties(RabbitMQProperties.class)
public class RabbitMQConfig {

    private final RabbitMQProperties props;

    public RabbitMQConfig(RabbitMQProperties props) {
        this.props = props;
    }

    @Bean
    public TopicExchange notificationsExchange() {
        return new TopicExchange(props.getExchange(), true, false);
    }

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(props.getQueue())
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", props.getDlq())
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(props.getDlq()).build();
    }

    @Bean
    public Binding binding() {
        return BindingBuilder
                .bind(notificationsQueue())
                .to(notificationsExchange())
                .with(props.getRoutingKey());
    }
}