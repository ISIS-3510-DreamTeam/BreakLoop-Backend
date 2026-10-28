package com.dreamteam.breakloop.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Decides what a batch upsert writes, given the documents already stored.
 * Pure on purpose: it runs inside a Firestore transaction (which may retry)
 * and is unit-tested without Firestore.
 */
final class UpsertPlanner {

    record Plan<T>(List<T> toWrite, List<String> skipped) {
    }

    private UpsertPlanner() {
    }

    /**
     * @param resolver called only when a document with the same id exists
     *                 (stored, or written earlier in this same batch);
     *                 returns the value to store, or empty to skip the item
     */
    static <T> Plan<T> plan(
            Map<String, T> stored,
            List<T> incoming,
            Function<T, String> id,
            BiFunction<T, T, Optional<T>> resolver) {

        Map<String, T> current = new HashMap<>(stored);
        List<T> toWrite = new ArrayList<>();
        List<String> skipped = new ArrayList<>();

        for (T item : incoming) {
            T existing = current.get(id.apply(item));
            Optional<T> result = existing == null
                    ? Optional.of(item)
                    : resolver.apply(existing, item);

            if (result.isEmpty()) {
                skipped.add(id.apply(item));
                continue;
            }

            current.put(id.apply(item), result.get());
            toWrite.add(result.get());
        }

        return new Plan<>(toWrite, skipped);
    }
}
