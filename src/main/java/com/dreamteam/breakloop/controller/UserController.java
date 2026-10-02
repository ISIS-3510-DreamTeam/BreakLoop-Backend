package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.model.Progress;
import com.dreamteam.breakloop.model.User;
import com.dreamteam.breakloop.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/{uid}")
    public User getUser(@PathVariable String uid) {
        return service.getUser(uid);
    }

    @PostMapping
    public void createUser(@RequestBody User user) {
        service.saveUser(user);
    }

    @GetMapping("/{uid}/progress")
    public Progress getProgress(@PathVariable String uid) {
        return service.getProgress(uid);
    }

    @PutMapping("/{uid}/progress")
    public void updateProgress(
            @PathVariable String uid,
            @RequestBody Progress progress) {
        service.saveProgress(uid, progress);
    }
}