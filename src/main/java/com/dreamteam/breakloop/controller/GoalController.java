package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.model.Goal;
import com.dreamteam.breakloop.repository.GoalRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{uid}/goals")
public class GoalController {

    private final GoalRepository repository;

    public GoalController(GoalRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Goal> getGoals(@PathVariable String uid) {
        return repository.findAll(uid);
    }

    @GetMapping("/{id}")
    public Goal getGoal(
            @PathVariable String uid,
            @PathVariable String id) {
        return repository.findById(uid, id)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
    }

    @PostMapping
    public void saveGoal(
            @PathVariable String uid,
            @RequestBody Goal goal) {
        repository.save(uid, goal);
    }
}