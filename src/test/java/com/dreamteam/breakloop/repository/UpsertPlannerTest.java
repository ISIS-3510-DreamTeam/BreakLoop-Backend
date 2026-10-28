package com.dreamteam.breakloop.repository;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpsertPlannerTest {

    // item = "id:state"; a closed item rejects any later "open" for its id
    private static String id(String item) {
        return item.substring(0, item.indexOf(':'));
    }

    private static Optional<String> resolve(String existing, String incoming) {
        return existing.endsWith(":closed") && incoming.endsWith(":open")
                ? Optional.empty()
                : Optional.of(incoming);
    }

    @Test
    void writesNewItemsAndSkipsRejectedOnes() {
        var plan = UpsertPlanner.plan(
                Map.of("a", "a:closed"),
                List.of("a:open", "b:open"),
                UpsertPlannerTest::id, UpsertPlannerTest::resolve);

        assertEquals(List.of("b:open"), plan.toWrite());
        assertEquals(List.of("a"), plan.skipped());
    }

    @Test
    void seesEarlierItemsOfTheSameBatch() {
        var plan = UpsertPlanner.plan(
                Map.of(),
                List.of("a:closed", "a:open"),
                UpsertPlannerTest::id, UpsertPlannerTest::resolve);

        assertEquals(List.of("a:closed"), plan.toWrite());
        assertEquals(List.of("a"), plan.skipped());
    }

    @Test
    void laterWriteWinsWhenAllowed() {
        var plan = UpsertPlanner.plan(
                Map.of("a", "a:open"),
                List.of("a:closed"),
                UpsertPlannerTest::id, UpsertPlannerTest::resolve);

        assertEquals(List.of("a:closed"), plan.toWrite());
    }
}
