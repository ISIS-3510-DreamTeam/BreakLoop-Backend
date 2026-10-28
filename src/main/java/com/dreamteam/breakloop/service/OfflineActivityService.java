package com.dreamteam.breakloop.service;

import com.dreamteam.breakloop.model.OfflineActivity;
import com.dreamteam.breakloop.repository.OfflineActivityRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class OfflineActivityService {

    private static final Set<String> CATEGORIES =
            Set.of("MOVE", "CREATE", "RELAX", "SOCIALIZE");

    private final OfflineActivityRepository repository;
    private final ObjectMapper objectMapper;
    private final Resource seedResource;

    public OfflineActivityService(
            OfflineActivityRepository repository,
            ObjectMapper objectMapper,
            @Value("classpath:catalog/activities.json") Resource seedResource
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.seedResource = seedResource;
    }

    public List<OfflineActivity> getActivities() {
        return repository.findAll();
    }

    public List<OfflineActivity> getActivities(String category) {

        if (category == null || category.isBlank()) {
            return repository.findAll();
        }

        return repository.findByCategory(
                category.trim().toUpperCase()
        );
    }

    /**
     * Version of the active catalog. {@code version} is a hash of the
     * catalog content, so it changes on any edit or deactivation and not
     * when the same catalog is seeded again; {@code updatedAt} is the latest
     * modification time.
     */
    public Map<String, Object> getCatalogVersion() {

        List<OfflineActivity> activities =
                repository.findAll()
                        .stream()
                        .sorted(Comparator.comparing(OfflineActivity::getId))
                        .toList();

        long updatedAt =
                activities.stream()
                        .mapToLong(OfflineActivity::getUpdatedAt)
                        .max()
                        .orElse(0L);

        try {

            byte[] digest =
                    MessageDigest.getInstance("SHA-256").digest(
                            objectMapper
                                    .writeValueAsString(activities)
                                    .getBytes(StandardCharsets.UTF_8)
                    );

            return Map.of(
                    "version",
                    HexFormat.of().formatHex(digest, 0, 8),
                    "updatedAt",
                    updatedAt
            );

        } catch (NoSuchAlgorithmException | IOException e) {

            throw new RuntimeException(
                    "Error computing catalog version",
                    e
            );
        }
    }

    /** Upserts the bundled catalog by id. Returns how many were written. */
    public int seedCatalog() {

        List<OfflineActivity> activities;

        try (InputStream in = seedResource.getInputStream()) {

            activities = objectMapper.readValue(
                    in,
                    new TypeReference<List<OfflineActivity>>() { }
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Error reading activity catalog",
                    e
            );
        }

        for (OfflineActivity activity : activities) {

            if (activity.getId() == null || activity.getId().isBlank()) {
                throw new IllegalArgumentException(
                        "Catalog activity without id"
                );
            }

            if (!CATEGORIES.contains(activity.getCategory())) {
                throw new IllegalArgumentException(
                        "Invalid category for " + activity.getId()
                );
            }

            normalize(activity);
            activity.setActive(true);
        }

        repository.upsertAll(activities);

        return activities.size();
    }

    public OfflineActivity getActivity(String id) {

        return repository
                .findById(id)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Offline activity not found"
                        )
                );
    }

    public String createActivity(
            OfflineActivity activity
    ) {

        normalize(activity);
        validateActivity(activity);

        return repository.save(activity);
    }

    public void updateActivity(
            String id,
            OfflineActivity activity
    ) {

        normalize(activity);
        validateActivity(activity);

        repository.update(id, activity);
    }

    public void deleteActivity(String id) {
        repository.delete(id);
    }

    // durationMin and prompt are the official fields. durationMinutes and
    // description are only kept filled as mirrors for older readers; when
    // both are sent, the official one wins.
    private void normalize(OfflineActivity activity) {

        if (activity.getDurationMin() <= 0) {
            activity.setDurationMin(activity.getDurationMinutes());
        }

        activity.setDurationMinutes(activity.getDurationMin());

        if (activity.getPrompt() == null || activity.getPrompt().isBlank()) {
            activity.setPrompt(activity.getDescription());
        }

        activity.setDescription(activity.getPrompt());

        activity.setUpdatedAt(System.currentTimeMillis());
    }

    private void validateActivity(
            OfflineActivity activity
    ) {

        if (activity.getTitle() == null ||
                activity.getTitle().isBlank()) {

            throw new IllegalArgumentException(
                    "Title is required"
            );
        }

        boolean hasText =
                (activity.getDescription() != null &&
                        !activity.getDescription().isBlank()) ||
                (activity.getPrompt() != null &&
                        !activity.getPrompt().isBlank());

        if (!hasText) {

            throw new IllegalArgumentException(
                    "Description or prompt is required"
            );
        }

        if (activity.getDurationMinutes() <= 0) {

            throw new IllegalArgumentException(
                    "Duration must be greater than zero"
            );
        }
    }
}