package com.dreamteam.breakloop.repository;

import com.dreamteam.breakloop.model.OfflineActivity;

import java.util.List;
import java.util.Optional;

public interface OfflineActivityRepository {

    List<OfflineActivity> findAll();

    List<OfflineActivity> findByCategory(String category);

    Optional<OfflineActivity> findById(String id);

    String save(OfflineActivity activity);

    void update(String id, OfflineActivity activity);

    void upsertAll(List<OfflineActivity> activities);

    void delete(String id);
}