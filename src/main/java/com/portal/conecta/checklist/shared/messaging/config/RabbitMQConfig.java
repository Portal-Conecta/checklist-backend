    package com.portal.conecta.checklist.shared.messaging.config;

    import org.springframework.amqp.core.Binding;
    import org.springframework.amqp.core.BindingBuilder;
    import org.springframework.amqp.core.Queue;
    import org.springframework.amqp.core.QueueBuilder;
    import org.springframework.amqp.core.TopicExchange;
    import org.springframework.amqp.rabbit.annotation.EnableRabbit;
    import org.springframework.amqp.rabbit.connection.ConnectionFactory;
    import org.springframework.amqp.rabbit.core.RabbitAdmin;
    import org.springframework.amqp.rabbit.core.RabbitTemplate;
    import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
    import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
    import org.springframework.boot.context.properties.EnableConfigurationProperties;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;

    @Configuration
    @EnableRabbit
    @EnableConfigurationProperties(RabbitMQProperties.class)
    @ConditionalOnProperty(prefix = "app.rabbitmq", name = "enabled", havingValue = "true", matchIfMissing = true)
    public class RabbitMQConfig {

        private final RabbitMQProperties props;

        public RabbitMQConfig(RabbitMQProperties props) {
            this.props = props;
        }

        @Bean
        public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
            return new RabbitAdmin(connectionFactory);
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
        public Binding binding(Queue notificationsQueue, TopicExchange notificationsExchange) {
            return BindingBuilder
                    .bind(notificationsQueue)
                    .to(notificationsExchange)
                    .with(props.getRoutingKey());
        }

        @Bean
        public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
            return new JacksonJsonMessageConverter();
        }

        @Bean
        public RabbitTemplate rabbitTemplate(
                ConnectionFactory connectionFactory,
                JacksonJsonMessageConverter jacksonJsonMessageConverter
        ) {
            RabbitTemplate template = new RabbitTemplate(connectionFactory);
            template.setMessageConverter(jacksonJsonMessageConverter);
            return template;
        }
    }
