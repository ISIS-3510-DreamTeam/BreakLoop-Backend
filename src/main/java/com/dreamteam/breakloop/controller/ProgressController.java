package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.model.Progress;
import com.dreamteam.breakloop.model.ProgressUpdate;
import com.dreamteam.breakloop.service.ProgressService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{uid}/progress")
public class ProgressController {

    private final ProgressService service;

    public ProgressController(ProgressService service) {
        this.service = service;
    }

    @GetMapping
    public Progress get(@PathVariable String uid) {
        return service.get(uid);
    }

    @PutMapping
    public void save(@PathVariable String uid, @Valid @RequestBody ProgressUpdate update) {
        service.save(uid, update);
    }
}