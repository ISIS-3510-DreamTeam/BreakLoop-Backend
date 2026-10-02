package com.dreamteam.breakloop.repository;

import com.dreamteam.breakloop.model.FocusSession;

import java.util.List;
import java.util.Optional;

public interface FocusSessionRepository {

    List<FocusSession> findAll(String uid);

    Optional<FocusSession> findById(String uid, String id);

    void save(String uid, FocusSession session);
}