package com.dreamteam.breakloop.repository;

import com.dreamteam.breakloop.model.ActivityCategory;
import com.dreamteam.breakloop.model.ActivityLog;
import com.dreamteam.breakloop.model.LogSource;
import com.dreamteam.breakloop.model.PlaceLabel;
import com.dreamteam.breakloop.model.TimeOfDay;
import com.dreamteam.breakloop.model.Weather;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static com.dreamteam.breakloop.repository.FirestoreFields.BATCH_SIZE;
import static com.dreamteam.breakloop.repository.FirestoreFields.getEnum;
import static com.dreamteam.breakloop.repository.FirestoreFields.put;

@Repository
public class FirebaseActivityLogRepository implements ActivityLogRepository {

    private final Firestore firestore;

    public FirebaseActivityLogRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public List<ActivityLog> findAll(String uid, Long since, Integer limit) {
        try {
            Query query = collection(uid)
                    .orderBy("startedAt", Query.Direction.DESCENDING);

            if (since != null) {
                query = query.whereGreaterThanOrEqualTo("startedAt", since);
            }

            if (limit != null) {
                query = query.limit(limit);
            }

            List<ActivityLog> result = new ArrayList<>();

            for (var document : query.get().get().getDocuments()) {
                result.add(fromDocument(document));
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving activity logs", e);
        }
    }

    @Override
    public Optional<ActivityLog> findById(String uid, String id) {
        try {
            DocumentSnapshot document =
                    collection(uid).document(id).get().get();

            return document.exists()
                    ? Optional.of(fromDocument(document))
                    : Optional.empty();

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving activity log", e);
        }
    }

    @Override
    public Optional<ActivityLog> upsert(
            String uid, ActivityLog incoming,
            BiFunction<ActivityLog, ActivityLog, Optional<ActivityLog>> resolver) {
        try {
            return firestore.runTransaction(tx -> {
                DocumentReference ref =
                        collection(uid).document(incoming.id());
                DocumentSnapshot snapshot = tx.get(ref).get();

                ActivityLog toStore = incoming;

                if (snapshot.exists()) {
                    Optional<ActivityLog> resolved =
                            resolver.apply(fromDocument(snapshot), incoming);

                    if (resolved.isEmpty()) {
                        return Optional.<ActivityLog>empty();
                    }

                    toStore = resolved.get();
                }

                tx.set(ref, toMap(toStore));

                return Optional.of(toStore);
            }).get();

        } catch (Exception e) {
            throw new RuntimeException("Error saving log", e);
        }
    }

    @Override
    public List<String> upsertAll(
            String uid, List<ActivityLog> incoming,
            BiFunction<ActivityLog, ActivityLog, Optional<ActivityLog>> resolver) {
        try {
            List<String> skipped = new ArrayList<>();

            for (int i = 0; i < incoming.size(); i += BATCH_SIZE) {
                List<ActivityLog> chunk = incoming.subList(
                        i, Math.min(i + BATCH_SIZE, incoming.size()));

                skipped.addAll(firestore.runTransaction(tx -> {
                    DocumentReference[] refs = chunk.stream()
                            .map(ActivityLog::id)
                            .distinct()
                            .map(collection(uid)::document)
                            .toArray(DocumentReference[]::new);

                    Map<String, ActivityLog> stored = new HashMap<>();

                    for (DocumentSnapshot snapshot : tx.getAll(refs).get()) {
                        if (snapshot.exists()) {
                            stored.put(snapshot.getId(), fromDocument(snapshot));
                        }
                    }

                    UpsertPlanner.Plan<ActivityLog> plan = UpsertPlanner.plan(
                            stored, chunk, ActivityLog::id, resolver);

                    for (ActivityLog item : plan.toWrite()) {
                        tx.set(collection(uid).document(item.id()), toMap(item));
                    }

                    return plan.skipped();
                }).get());
            }

            return skipped;

        } catch (Exception e) {
            throw new RuntimeException("Error saving logs", e);
        }
    }

    private CollectionReference collection(String uid) {
        return firestore
                .collection("users")
                .document(uid)
                .collection("activityLogs");
    }

    private Map<String, Object> toMap(ActivityLog l) {
        Map<String, Object> map = new LinkedHashMap<>();

        put(map, "id", l.id());
        put(map, "activityId", l.activityId());
        put(map, "category", l.category());
        put(map, "source", l.source());
        put(map, "interactionId", l.interactionId());
        put(map, "startedAt", l.startedAt());
        put(map, "completedAt", l.completedAt());
        put(map, "xp", l.xp());
        put(map, "timeOfDay", l.timeOfDay());
        put(map, "weather", l.weather());
        put(map, "availableMin", l.availableMin());
        put(map, "placeLabel", l.placeLabel());
        put(map, "latitude", l.latitude());
        put(map, "longitude", l.longitude());

        return map;
    }

    private ActivityLog fromDocument(DocumentSnapshot d) {
        Long xp = d.getLong("xp");
        Long availableMin = d.getLong("availableMin");

        return new ActivityLog(
                d.getId(),
                d.getString("activityId"),
                getEnum(d, "category", ActivityCategory.class),
                getEnum(d, "source", LogSource.class),
                d.getString("interactionId"),
                d.getLong("startedAt"),
                d.getLong("completedAt"),
                xp != null ? xp.intValue() : 0,
                getEnum(d, "timeOfDay", TimeOfDay.class),
                getEnum(d, "weather", Weather.class),
                availableMin != null ? availableMin.intValue() : 0,
                getEnum(d, "placeLabel", PlaceLabel.class),
                d.getDouble("latitude"),
                d.getDouble("longitude")
        );
    }
}
