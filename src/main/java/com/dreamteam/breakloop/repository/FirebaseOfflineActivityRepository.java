package com.dreamteam.breakloop.repository;

import com.dreamteam.breakloop.model.OfflineActivity;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class FirebaseOfflineActivityRepository
        implements OfflineActivityRepository {

    private final Firestore firestore;

    private static final String COLLECTION =
            "offlineActivities";

    private static final int BATCH_SIZE = 500;

    public FirebaseOfflineActivityRepository(
            Firestore firestore
    ) {
        this.firestore = firestore;
    }

    @Override
    public List<OfflineActivity> findAll() {
        return queryActive(null);
    }

    @Override
    public List<OfflineActivity> findByCategory(String category) {
        return queryActive(category);
    }

    private List<OfflineActivity> queryActive(String category) {

        try {

            Query query =
                    firestore
                            .collection(COLLECTION)
                            .whereEqualTo("active", true);

            if (category != null) {
                query = query.whereEqualTo("category", category);
            }

            List<QueryDocumentSnapshot> documents =
                    query.get().get().getDocuments();

            List<OfflineActivity> activities =
                    new ArrayList<>();

            for (QueryDocumentSnapshot document : documents) {

                activities.add(
                        documentToActivity(document)
                );
            }

            return activities;

        } catch (InterruptedException | ExecutionException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Error retrieving offline activities",
                    e
            );
        }
    }

    @Override
    public Optional<OfflineActivity> findById(
            String id
    ) {

        try {

            DocumentSnapshot document =
                    firestore
                            .collection(COLLECTION)
                            .document(id)
                            .get()
                            .get();

            if (!document.exists()) {
                return Optional.empty();
            }

            return Optional.of(
                    documentToActivity(document)
            );

        } catch (InterruptedException | ExecutionException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Error retrieving offline activity",
                    e
            );
        }
    }

    @Override
    public String save(
            OfflineActivity activity
    ) {

        try {

            DocumentReference document =
                    firestore
                            .collection(COLLECTION)
                            .document();

            activity.setId(document.getId());

            document.set(
                    activityToMap(activity)
            ).get();

            return document.getId();

        } catch (InterruptedException | ExecutionException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Error saving offline activity",
                    e
            );
        }
    }

    @Override
    public void update(
            String id,
            OfflineActivity activity
    ) {

        try {

            activity.setId(id);

            firestore
                    .collection(COLLECTION)
                    .document(id)
                    .set(activityToMap(activity))
                    .get();

        } catch (InterruptedException | ExecutionException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Error updating offline activity",
                    e
            );
        }
    }

    @Override
    public void upsertAll(List<OfflineActivity> activities) {

        try {

            for (int i = 0; i < activities.size(); i += BATCH_SIZE) {

                WriteBatch batch = firestore.batch();

                for (OfflineActivity activity : activities.subList(
                        i, Math.min(i + BATCH_SIZE, activities.size()))) {

                    batch.set(
                            firestore
                                    .collection(COLLECTION)
                                    .document(activity.getId()),
                            activityToMap(activity)
                    );
                }

                batch.commit().get();
            }

        } catch (InterruptedException | ExecutionException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Error seeding offline activities",
                    e
            );
        }
    }

    @Override
    public void delete(
            String id
    ) {

        try {

            firestore
                    .collection(COLLECTION)
                    .document(id)
                    .update(
                            "active", false,
                            "updatedAt", System.currentTimeMillis()
                    )
                    .get();

        } catch (InterruptedException | ExecutionException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Error deleting offline activity",
                    e
            );
        }
    }

    @SuppressWarnings("unchecked")
    private OfflineActivity documentToActivity(
            DocumentSnapshot document
    ) {

        OfflineActivity activity =
                new OfflineActivity();

        activity.setId(document.getId());

        activity.setTitle(
                document.getString("title")
        );

        activity.setDescription(
                document.getString("description")
        );

        activity.setCategory(
                document.getString("category")
        );

        Long duration =
                document.getLong("durationMinutes");

        activity.setDurationMinutes(
                duration != null ? duration.intValue() : 0
        );

        activity.setDifficulty(
                document.getString("difficulty")
        );

        Boolean active =
                document.getBoolean("active");

        activity.setActive(
                active != null && active
        );

        Long durationMin =
                document.getLong("durationMin");

        activity.setDurationMin(
                durationMin != null ? durationMin.intValue() : 0
        );

        Long xp = document.getLong("xp");

        activity.setXp(xp != null ? xp.intValue() : 0);

        activity.setPrompt(document.getString("prompt"));

        Boolean outdoor = document.getBoolean("outdoor");

        activity.setOutdoor(outdoor != null && outdoor);

        activity.setWeather(
                (List<String>) document.get("weather")
        );

        activity.setTimeOfDay(
                (List<String>) document.get("timeOfDay")
        );

        Long updatedAt = document.getLong("updatedAt");

        activity.setUpdatedAt(
                updatedAt != null ? updatedAt : 0L
        );

        return activity;
    }

    // Null fields are omitted: a catalog item has no description/difficulty,
    // a legacy item has no prompt/weather.
    private Map<String, Object> activityToMap(
            OfflineActivity activity
    ) {

        Map<String, Object> map = new HashMap<>();

        map.put("title", activity.getTitle());
        map.put("category", activity.getCategory());
        map.put("durationMinutes", activity.getDurationMinutes());
        map.put("durationMin", activity.getDurationMin());
        map.put("xp", activity.getXp());
        map.put("outdoor", activity.isOutdoor());
        map.put("active", activity.isActive());
        map.put("updatedAt", activity.getUpdatedAt());

        putIfNotNull(map, "description", activity.getDescription());
        putIfNotNull(map, "difficulty", activity.getDifficulty());
        putIfNotNull(map, "prompt", activity.getPrompt());
        putIfNotNull(map, "weather", activity.getWeather());
        putIfNotNull(map, "timeOfDay", activity.getTimeOfDay());

        return map;
    }

    private void putIfNotNull(
            Map<String, Object> map,
            String key,
            Object value
    ) {
        if (value != null) {
            map.put(key, value);
        }
    }
}
