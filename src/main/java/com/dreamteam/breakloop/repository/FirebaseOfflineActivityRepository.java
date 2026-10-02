package com.dreamteam.breakloop.repository;

import com.dreamteam.breakloop.model.OfflineActivity;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class FirebaseOfflineActivityRepository
        implements OfflineActivityRepository {

    private final Firestore firestore;

    private static final String COLLECTION =
            "offlineActivities";

    public FirebaseOfflineActivityRepository(
            Firestore firestore
    ) {
        this.firestore = firestore;
    }

    @Override
    public List<OfflineActivity> findAll() {

        try {

            ApiFuture<QuerySnapshot> future =
                    firestore
                            .collection(COLLECTION)
                            .whereEqualTo("active", true)
                            .get();

            List<QueryDocumentSnapshot> documents =
                    future.get().getDocuments();

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
    public void delete(
            String id
    ) {

        try {

            firestore
                    .collection(COLLECTION)
                    .document(id)
                    .update("active", false)
                    .get();

        } catch (InterruptedException | ExecutionException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Error deleting offline activity",
                    e
            );
        }
    }

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

        return activity;
    }

    private java.util.Map<String, Object> activityToMap(
            OfflineActivity activity
    ) {

        return java.util.Map.of(
                "title", activity.getTitle(),
                "description", activity.getDescription(),
                "category", activity.getCategory(),
                "durationMinutes",
                activity.getDurationMinutes(),
                "difficulty", activity.getDifficulty(),
                "active", activity.isActive()
        );
    }
}