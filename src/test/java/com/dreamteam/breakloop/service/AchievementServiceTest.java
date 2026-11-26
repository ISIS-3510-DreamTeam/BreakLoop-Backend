package com.dreamteam.breakloop.service;

import com.dreamteam.breakloop.model.Achievement;
import com.dreamteam.breakloop.model.AchievementRuleType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AchievementServiceTest {

    @Test
    void bundledCatalogHas5Achievements() {
        AchievementService service = new AchievementService(
                new ObjectMapper(), new ClassPathResource("catalog/achievements.json"));

        assertEquals(5, service.getCatalog().size());
    }

    @Test
    void streakAchievementsHaveTheirTargets() {
        AchievementService service = new AchievementService(
                new ObjectMapper(), new ClassPathResource("catalog/achievements.json"));

        Achievement streak7 = find(service, "streak_7");
        Achievement streak30 = find(service, "streak_30");

        assertEquals(AchievementRuleType.STREAK_DAYS, streak7.getRule());
        assertEquals(7, streak7.getTarget());
        assertEquals(30, streak30.getTarget());
    }

    @Test
    void rejectsDuplicateIds() {
        String json = """
                [
                  {"id":"a","name":"A","description":"d","image":"achievements/a.png",
                   "rule":"STREAK_DAYS","target":7},
                  {"id":"a","name":"B","description":"d","image":"achievements/b.png",
                   "rule":"STREAK_DAYS","target":7}
                ]
                """;

        assertThrows(IllegalStateException.class, () -> load(json));
    }

    @Test
    void rejectsZeroTarget() {
        String json = """
                [
                  {"id":"a","name":"A","description":"d","image":"achievements/a.png",
                   "rule":"STREAK_DAYS","target":0}
                ]
                """;

        assertThrows(IllegalStateException.class, () -> load(json));
    }

    @Test
    void rejectsUnknownRule() {
        String json = """
                [
                  {"id":"a","name":"A","description":"d","image":"achievements/a.png",
                   "rule":"NO_SUCH_RULE","target":7}
                ]
                """;

        assertThrows(RuntimeException.class, () -> load(json));
    }

    private static AchievementService load(String json) {
        return new AchievementService(
                new ObjectMapper(),
                new ByteArrayResource(json.getBytes(StandardCharsets.UTF_8)));
    }

    private static Achievement find(AchievementService service, String id) {
        return service.getCatalog().stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }
}