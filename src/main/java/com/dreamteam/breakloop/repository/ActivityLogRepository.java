package com.dreamteam.breakloop.repository;

import com.dreamteam.breakloop.model.ActivityLog;

import java.util.List;
import java.util.function.BiFunction;
import java.util.Optional;

public interface ActivityLogRepository {

    /** Newest first; {@code since} filters on startedAt, null limit = all. */
    List<ActivityLog> findAll(String uid, Long since, Integer limit);

    Optional<ActivityLog> findById(String uid, String id);

    /**
     * Atomic read-then-write (Firestore transaction), so concurrent retries
     * of the same id cannot interleave. The resolver gets the stored and the
     * incoming value and returns what to store, or empty to reject.
     *
     * @return the stored value, or empty if the resolver rejected it
     */
    Optional<ActivityLog> upsert(
            String uid, ActivityLog incoming,
            BiFunction<ActivityLog, ActivityLog, Optional<ActivityLog>> resolver);

    /**
     * Same as {@link #upsert} for many items, one transaction per 500.
     *
     * @return ids of the items the resolver rejected
     */
    List<String> upsertAll(
            String uid, List<ActivityLog> incoming,
            BiFunction<ActivityLog, ActivityLog, Optional<ActivityLog>> resolver);
}
