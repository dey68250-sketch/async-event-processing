package com.example.asyncevents.controller;

import com.example.asyncevents.model.EventMessage;
import com.example.asyncevents.service.EventPublisher;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventPublisher eventPublisher;

    public EventController(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> publishEvent(@Valid @RequestBody EventMessage eventMessage) {
        eventPublisher.publish(eventMessage);
        return Map.of(
                "message", "Event accepted for processing",
                "eventId", eventMessage.eventId()
        );
    }
}
