package com.portal.conecta.checklist.integration.shared.messaging;

import com.portal.conecta.checklist.shared.messaging.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RabbitMQConfigIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(RabbitMQConfig.class, ConnectionFactoryTestConfig.class)
            .withPropertyValues(
                    "app.rabbitmq.enabled=true",
                    "app.rabbitmq.exchange=notifications.exchange",
                    "app.rabbitmq.queue=notifications.queue",
                    "app.rabbitmq.dlq=notifications.queue.dlq",
                    "app.rabbitmq.routing-key=notification.requested"
            );

    @Test
    void shouldCreateRabbitInfrastructureBeansFromConfiguredProperties() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RabbitAdmin.class);
            assertThat(context).hasSingleBean(JacksonJsonMessageConverter.class);
            assertThat(context).hasSingleBean(RabbitTemplate.class);

            TopicExchange exchange = context.getBean(TopicExchange.class);
            Queue notificationsQueue = context.getBean("notificationsQueue", Queue.class);
            Queue deadLetterQueue = context.getBean("deadLetterQueue", Queue.class);
            Binding binding = context.getBean(Binding.class);

            assertThat(exchange.getName()).isEqualTo("notifications.exchange");
            assertThat(exchange.isDurable()).isTrue();
            assertThat(exchange.isAutoDelete()).isFalse();

            assertThat(notificationsQueue.getName()).isEqualTo("notifications.queue");
            assertThat(notificationsQueue.isDurable()).isTrue();
            assertThat(notificationsQueue.getArguments())
                    .containsEntry("x-dead-letter-exchange", "")
                    .containsEntry("x-dead-letter-routing-key", "notifications.queue.dlq");

            assertThat(deadLetterQueue.getName()).isEqualTo("notifications.queue.dlq");
            assertThat(deadLetterQueue.isDurable()).isTrue();

            assertThat(binding.getDestination()).isEqualTo("notifications.queue");
            assertThat(binding.getExchange()).isEqualTo("notifications.exchange");
            assertThat(binding.getRoutingKey()).isEqualTo("notification.requested");

            RabbitTemplate rabbitTemplate = context.getBean(RabbitTemplate.class);
            assertThat(rabbitTemplate.getMessageConverter()).isInstanceOf(JacksonJsonMessageConverter.class);
        });
    }

    @Test
    void shouldNotCreateRabbitInfrastructureWhenMessagingIsDisabled() {
        new ApplicationContextRunner()
                .withUserConfiguration(RabbitMQConfig.class, ConnectionFactoryTestConfig.class)
                .withPropertyValues("app.rabbitmq.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(RabbitAdmin.class);
                    assertThat(context).doesNotHaveBean(TopicExchange.class);
                    assertThat(context).doesNotHaveBean(RabbitTemplate.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class ConnectionFactoryTestConfig {

        @Bean
        ConnectionFactory connectionFactory() {
            return mock(ConnectionFactory.class);
        }
    }
}
