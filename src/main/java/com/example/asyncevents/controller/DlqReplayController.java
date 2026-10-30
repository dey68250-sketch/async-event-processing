
package com.example.asyncevents.controller;

import com.example.asyncevents.service.DlqReplayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dlq")
public class DlqReplayController {

    private final DlqReplayService dlqReplayService;

    public DlqReplayController(DlqReplayService dlqReplayService) {
        this.dlqReplayService = dlqReplayService;
    }

    @PostMapping("/replay")
    public ResponseEntity<Map<String, String>> replayOne() throws Exception {

        boolean replayed = dlqReplayService.readOneMessage();

        if (!replayed) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(
                Map.of("message", "DLQ message replayed successfully")
        );
    }
}
