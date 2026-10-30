package com.example.asyncevents.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {
    public static final String QUEUE_NAME = "tracking.event.queue";

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }

    @Bean
    public org.springframework.boot.CommandLineRunner declareQueue(
            org.springframework.amqp.rabbit.core.RabbitAdmin rabbitAdmin) {
        return args -> rabbitAdmin.initialize();
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("tracking.event.dlq").build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("tracking.event.dlx");
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("tracking.event.dead");
    }

    @Bean
    public Queue trackingEventQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .deadLetterExchange("tracking.event.dlx")
                .deadLetterRoutingKey("tracking.event.dead")
                .build();
    }
}
