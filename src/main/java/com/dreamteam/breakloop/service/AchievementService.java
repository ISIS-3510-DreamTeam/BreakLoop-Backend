package com.dreamteam.breakloop.service;

import com.dreamteam.breakloop.model.Achievement;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AchievementService {

    private static final String DEFAULT_CATALOG = "catalog/achievements.json";

    private final List<Achievement> catalog;

    @Autowired
    public AchievementService(ObjectMapper objectMapper) {
        this(objectMapper, new ClassPathResource(DEFAULT_CATALOG));
    }

    // Package-private so tests can load another catalog file.
    AchievementService(ObjectMapper objectMapper, Resource resource) {
        this.catalog = List.copyOf(load(objectMapper, resource));
    }

    /** Achievements in catalog order. Read once at startup. */
    public List<Achievement> getCatalog() {
        return catalog;
    }

    private static List<Achievement> load(ObjectMapper objectMapper, Resource resource) {
        List<Achievement> achievements;

        try (InputStream input = resource.getInputStream()) {
            achievements = objectMapper.readValue(
                    input, new TypeReference<List<Achievement>>() {});
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the achievements catalog", e);
        }

        validate(achievements);
        return achievements;
    }

    private static void validate(List<Achievement> achievements) {
        Set<String> ids = new HashSet<>();

        for (Achievement achievement : achievements) {
            if (isBlank(achievement.getId())
                    || isBlank(achievement.getName())
                    || isBlank(achievement.getImage())
                    || achievement.getRule() == null
                    || achievement.getTarget() <= 0) {
                throw new IllegalStateException(
                        "Invalid achievement in catalog: " + achievement.getId());
            }

            if (!ids.add(achievement.getId())) {
                throw new IllegalStateException(
                        "Duplicate achievement id in catalog: " + achievement.getId());
            }
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}