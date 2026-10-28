package com.dreamteam.breakloop.repository;

import com.dreamteam.breakloop.model.SuggestionInteraction;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SuggestionInteractionRepository {

    /** Newest first; {@code since} filters on openedAt, null limit = all. */
    List<SuggestionInteraction> findAll(String uid, Long since, Integer limit);

    Optional<SuggestionInteraction> findById(String uid, String id);

    Map<String, SuggestionInteraction> findByIds(
            String uid, Collection<String> ids);

    void save(String uid, SuggestionInteraction interaction);

    void saveAll(String uid, List<SuggestionInteraction> interactions);
}
