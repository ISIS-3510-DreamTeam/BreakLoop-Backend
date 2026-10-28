package com.dreamteam.breakloop.repository;

import com.dreamteam.breakloop.model.SuggestionInteraction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/** Test double with the same upsert semantics as the Firestore one. */
public class InMemorySuggestionInteractionRepository
        implements SuggestionInteractionRepository {

    public final Map<String, SuggestionInteraction> stored =
            new ConcurrentHashMap<>();

    @Override
    public List<SuggestionInteraction> findAll(
            String uid, Long since, Integer limit) {
        return new ArrayList<>(stored.values());
    }

    @Override
    public Optional<SuggestionInteraction> findById(String uid, String id) {
        return Optional.ofNullable(stored.get(id));
    }

    @Override
    public Optional<SuggestionInteraction> upsert(
            String uid, SuggestionInteraction incoming,
            BiFunction<SuggestionInteraction, SuggestionInteraction,
                    Optional<SuggestionInteraction>> resolver) {
        List<String> skipped = upsertAll(uid, List.of(incoming), resolver);
        return skipped.isEmpty()
                ? Optional.of(stored.get(incoming.id()))
                : Optional.empty();
    }

    @Override
    public synchronized List<String> upsertAll(
            String uid, List<SuggestionInteraction> incoming,
            BiFunction<SuggestionInteraction, SuggestionInteraction,
                    Optional<SuggestionInteraction>> resolver) {
        var plan = UpsertPlanner.plan(
                stored, incoming, SuggestionInteraction::id, resolver);
        plan.toWrite().forEach(i -> stored.put(i.id(), i));
        return plan.skipped();
    }
}
