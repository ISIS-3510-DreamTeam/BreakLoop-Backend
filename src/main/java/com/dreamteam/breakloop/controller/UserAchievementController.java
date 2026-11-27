package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.exception.NotFoundException;
import com.dreamteam.breakloop.model.AchievementUnlockRequest;
import com.dreamteam.breakloop.model.UserAchievement;
import com.dreamteam.breakloop.service.UserAchievementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/{uid}/achievements")
public class UserAchievementController {

    private final UserAchievementService service;

    public UserAchievementController(UserAchievementService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserAchievement> getUnlocked(@PathVariable String uid) {
        return service.getUnlocked(uid);
    }

    @PutMapping("/{achievementId}")
    public UserAchievement unlock(
            @PathVariable String uid,
            @PathVariable String achievementId,
            @Valid @RequestBody AchievementUnlockRequest request) {
        return service.unlock(uid, achievementId, request.getUnlockedAt());
    }

    /** Developer tools only. Clears the cloud copy of the unlocks. */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll(@PathVariable String uid) {
        service.deleteAll(uid);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Not Found", "message", e.getMessage()));
    }
}