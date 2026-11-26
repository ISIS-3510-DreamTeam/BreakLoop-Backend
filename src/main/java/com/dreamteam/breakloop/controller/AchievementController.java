package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.model.Achievement;
import com.dreamteam.breakloop.service.AchievementService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AchievementController {

    private final AchievementService service;

    public AchievementController(AchievementService service) {
        this.service = service;
    }

    /** Achievement catalog: name, description, image path and rule. Public, like /api/pets. */
    @GetMapping("/achievements")
    public List<Achievement> getAchievements() {
        return service.getCatalog();
    }
}