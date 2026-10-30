package com.example.asyncevents.service;

import com.example.asyncevents.config.RabbitMQConfig;
import com.example.asyncevents.model.EventMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(EventMessage eventMessage) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.QUEUE_NAME,
                eventMessage
        );
    }
}
