package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.model.FocusSession;
import com.dreamteam.breakloop.repository.FocusSessionRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{uid}/focus-sessions")
public class FocusSessionController {

    private final FocusSessionRepository repository;

    public FocusSessionController(FocusSessionRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<FocusSession> getSessions(@PathVariable String uid) {
        return repository.findAll(uid);
    }

    @GetMapping("/{id}")
    public FocusSession getSession(
            @PathVariable String uid,
            @PathVariable String id) {
        return repository.findById(uid, id)
                .orElseThrow(() -> new RuntimeException("Focus session not found"));
    }

    @PostMapping
    public void saveSession(
            @PathVariable String uid,
            @RequestBody FocusSession session) {
        repository.save(uid, session);
    }
}