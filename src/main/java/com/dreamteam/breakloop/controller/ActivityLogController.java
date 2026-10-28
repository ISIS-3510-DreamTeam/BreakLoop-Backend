package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.model.ActivityLog;
import com.dreamteam.breakloop.model.BatchResult;
import com.dreamteam.breakloop.service.ActivityLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{uid}/activity-logs")
public class ActivityLogController {

    private final ActivityLogService service;

    public ActivityLogController(ActivityLogService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ActivityLog>> getLogs(
            @PathVariable String uid,
            @RequestParam(required = false) Long since,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(service.getLogs(uid, since, limit));
    }

    @GetMapping("/{logId}")
    public ResponseEntity<ActivityLog> getLog(
            @PathVariable String uid,
            @PathVariable String logId
    ) {
        return ResponseEntity.ok(service.getLog(uid, logId));
    }

    @PutMapping("/{logId}")
    public ResponseEntity<ActivityLog> upsertLog(
            @PathVariable String uid,
            @PathVariable String logId,
            @RequestBody ActivityLog log
    ) {
        return ResponseEntity.ok(service.upsert(uid, logId, log));
    }

    @PostMapping("/batch")
    public ResponseEntity<BatchResult> upsertLogs(
            @PathVariable String uid,
            @RequestBody List<ActivityLog> logs
    ) {
        return ResponseEntity.ok(service.upsertAll(uid, logs));
    }
}
