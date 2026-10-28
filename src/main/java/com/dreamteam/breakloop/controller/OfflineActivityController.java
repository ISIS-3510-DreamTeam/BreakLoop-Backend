package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.model.OfflineActivity;
import com.dreamteam.breakloop.service.OfflineActivityService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/offline-activities")
public class OfflineActivityController {

    private final OfflineActivityService service;
    private final String adminKey;

    public OfflineActivityController(
            OfflineActivityService service,
            @Value("${breakloop.admin.key:}") String adminKey
    ) {
        this.service = service;
        this.adminKey = adminKey;
    }

    @GetMapping
    public ResponseEntity<List<OfflineActivity>> getActivities(
            @RequestParam(required = false) String category
    ) {

        return ResponseEntity.ok(
                service.getActivities(category)
        );
    }

    @GetMapping("/version")
    public ResponseEntity<Map<String, Object>> getCatalogVersion() {

        return ResponseEntity.ok(
                service.getCatalogVersion()
        );
    }

    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seedCatalog(
            @RequestHeader(value = "X-Admin-Key", required = false)
            String providedKey
    ) {

        // No configured key means seeding is disabled, never "open".
        if (adminKey.isBlank() || providedKey == null ||
                !MessageDigest.isEqual(
                        adminKey.getBytes(StandardCharsets.UTF_8),
                        providedKey.getBytes(StandardCharsets.UTF_8))) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        return ResponseEntity.ok(
                Map.of("seeded", service.seedCatalog())
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