package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.model.BatchResult;
import com.dreamteam.breakloop.model.SuggestionInteraction;
import com.dreamteam.breakloop.service.SuggestionInteractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{uid}/suggestion-interactions")
public class SuggestionInteractionController {

    private final SuggestionInteractionService service;

    public SuggestionInteractionController(
            SuggestionInteractionService service
    ) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<SuggestionInteraction>> getInteractions(
            @PathVariable String uid,
            @RequestParam(required = false) Long since,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(service.getInteractions(uid, since, limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuggestionInteraction> getInteraction(
            @PathVariable String uid,
            @PathVariable String id
    ) {
        return ResponseEntity.ok(service.getInteraction(uid, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuggestionInteraction> upsertInteraction(
            @PathVariable String uid,
            @PathVariable String id,
            @RequestBody SuggestionInteraction interaction
    ) {
        return ResponseEntity.ok(service.upsert(uid, id, interaction));
    }

    @PostMapping("/batch")
    public ResponseEntity<BatchResult> upsertInteractions(
            @PathVariable String uid,
            @RequestBody List<SuggestionInteraction> interactions
    ) {
        return ResponseEntity.ok(service.upsertAll(uid, interactions));
    }
}
