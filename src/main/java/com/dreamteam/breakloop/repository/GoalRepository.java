package com.dreamteam.breakloop.repository;

import com.dreamteam.breakloop.model.Goal;

import java.util.List;
import java.util.Optional;

public interface GoalRepository {

    List<Goal> findAll(String uid);

    Optional<Goal> findById(String uid, String id);

    void save(String uid, Goal goal);
}