package com.example.asyncevents.repository;

import com.example.asyncevents.entity.ProcessdEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessdEvent,String> {
    boolean existsByEventId(String eventId);
}
