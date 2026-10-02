package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.model.Usage;
import com.dreamteam.breakloop.repository.UsageRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{uid}/usage")
public class UsageController {

    private final UsageRepository repository;

    public UsageController(UsageRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Usage> getUsage(@PathVariable String uid) {
        return repository.findAll(uid);
    }

    @GetMapping("/{date}")
    public Usage getUsageByDate(
            @PathVariable String uid,
            @PathVariable String date) {
        return repository.findByDate(uid, date)
                .orElseThrow(() -> new RuntimeException("Usage not found"));
    }

    @PostMapping
    public void saveUsage(
            @PathVariable String uid,
            @RequestBody Usage usage) {
        repository.save(uid, usage);
    }
}