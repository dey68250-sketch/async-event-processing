package com.example.asyncevents.model;

import jakarta.validation.constraints.NotBlank;

public record EventMessage(
        @NotBlank String eventId,
        @NotBlank String eventType,
        @NotBlank String parcelId,
        @NotBlank String location
){}
