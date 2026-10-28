package com.dreamteam.breakloop.repository;

import com.dreamteam.breakloop.model.ActivityLog;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ActivityLogRepository {

    /** Newest first; {@code since} filters on startedAt, null limit = all. */
    List<ActivityLog> findAll(String uid, Long since, Integer limit);

    Optional<ActivityLog> findById(String uid, String id);

    Map<String, ActivityLog> findByIds(String uid, Collection<String> ids);

    void save(String uid, ActivityLog log);

    void saveAll(String uid, List<ActivityLog> logs);
}
