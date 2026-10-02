package com.dreamteam.breakloop.repository;

import com.dreamteam.breakloop.model.Usage;

import java.util.List;
import java.util.Optional;

public interface UsageRepository {

    List<Usage> findAll(String uid);

    Optional<Usage> findByDate(String uid, String date);

    void save(String uid, Usage usage);
}