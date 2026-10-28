package com.dreamteam.breakloop.repository;

import com.dreamteam.breakloop.model.ActivityLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/** Test double with the same upsert semantics as the Firestore one. */
public class InMemoryActivityLogRepository
        implements ActivityLogRepository {

    public final Map<String, ActivityLog> stored =
            new ConcurrentHashMap<>();

    @Override
    public List<ActivityLog> findAll(
            String uid, Long since, Integer limit) {
        return new ArrayList<>(stored.values());
    }

    @Override
    public Optional<ActivityLog> findById(String uid, String id) {
        return Optional.ofNullable(stored.get(id));
    }

    @Override
    public Optional<ActivityLog> upsert(
            String uid, ActivityLog incoming,
            BiFunction<ActivityLog, ActivityLog,
                    Optional<ActivityLog>> resolver) {
        List<String> skipped = upsertAll(uid, List.of(incoming), resolver);
        return skipped.isEmpty()
                ? Optional.of(stored.get(incoming.id()))
                : Optional.empty();
    }

    @Override
    public synchronized List<String> upsertAll(
            String uid, List<ActivityLog> incoming,
            BiFunction<ActivityLog, ActivityLog,
                    Optional<ActivityLog>> resolver) {
        var plan = UpsertPlanner.plan(
                stored, incoming, ActivityLog::id, resolver);
        plan.toWrite().forEach(i -> stored.put(i.id(), i));
        return plan.skipped();
    }
}
