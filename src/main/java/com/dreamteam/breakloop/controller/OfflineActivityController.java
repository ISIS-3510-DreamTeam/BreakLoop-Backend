package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.model.OfflineActivity;
import com.dreamteam.breakloop.service.OfflineActivityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/offline-activities")
public class OfflineActivityController {

    private final OfflineActivityService service;

    public OfflineActivityController(
            OfflineActivityService service
    ) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<OfflineActivity>> getActivities() {

        return ResponseEntity.ok(
                service.getActivities()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfflineActivity> getActivity(
            @PathVariable String id
    ) {

        return ResponseEntity.ok(
                service.getActivity(id)
        );
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createActivity(
            @RequestBody OfflineActivity activity
    ) {

        String id =
                service.createActivity(activity);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        Map.of(
                                "id", id,
                                "message",
                                "Activity created successfully"
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateActivity(
            @PathVariable String id,
            @RequestBody OfflineActivity activity
    ) {

        service.updateActivity(id, activity);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(
            @PathVariable String id
    ) {

        service.deleteActivity(id);

        return ResponseEntity.noContent().build();
    }
}