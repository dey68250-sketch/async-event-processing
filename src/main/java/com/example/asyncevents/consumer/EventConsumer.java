package com.example.asyncevents.consumer;

import com.example.asyncevents.config.RabbitMQConfig;
import com.example.asyncevents.entity.ProcessdEvent;
import com.example.asyncevents.model.EventMessage;
import com.example.asyncevents.repository.ProcessedEventRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class EventConsumer {

    private final ProcessedEventRepository processedEventRepository;

    public EventConsumer(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consume(EventMessage eventMessage) {
        ProcessdEvent processdEvent = new ProcessdEvent(
                eventMessage.eventId(),
                eventMessage.eventType(),
                eventMessage.parcelId(),
                eventMessage.location(),
                LocalDateTime.now()
        );


        if(processedEventRepository.existsByEventId(eventMessage.eventId())) {
            System.out.println(
                    "DUPLICATE EVENT SKIPPED: " + eventMessage.eventId()
            );
            return ;
        }

        ProcessdEvent savedEvent = processedEventRepository.save(processdEvent);

        System.out.println("========== EVENT SAVED TO MYSQL ==========");
        System.out.println("Event ID: " + savedEvent.getEventId());
        System.out.println("Event Type: " + savedEvent.getEventType());
        System.out.println("Parcel ID: " + savedEvent.getParcelId());
        System.out.println("Location: " + savedEvent.getLocation());
        System.out.println("==========================================");

    }

}
