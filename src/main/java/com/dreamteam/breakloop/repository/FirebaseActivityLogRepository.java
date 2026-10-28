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
import com.google.cloud.firestore.WriteBatch;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public Map<String, ActivityLog> findByIds(
            String uid, Collection<String> ids) {
        try {
            List<DocumentReference> refs = ids.stream()
                    .map(collection(uid)::document)
                    .toList();

            Map<String, ActivityLog> result = new HashMap<>();

            for (int i = 0; i < refs.size(); i += BATCH_SIZE) {
                var chunk = refs.subList(
                        i, Math.min(i + BATCH_SIZE, refs.size()));

                for (var document : firestore.getAll(
                        chunk.toArray(new DocumentReference[0])).get()) {
                    if (document.exists()) {
                        result.put(document.getId(), fromDocument(document));
                    }
                }
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving activity logs", e);
        }
    }

    @Override
    public void save(String uid, ActivityLog log) {
        try {
            collection(uid).document(log.id()).set(toMap(log)).get();

        } catch (Exception e) {
            throw new RuntimeException("Error saving activity log", e);
        }
    }

    @Override
    public void saveAll(String uid, List<ActivityLog> logs) {
        try {
            for (int i = 0; i < logs.size(); i += BATCH_SIZE) {
                WriteBatch batch = firestore.batch();

                for (var log : logs.subList(
                        i, Math.min(i + BATCH_SIZE, logs.size()))) {
                    batch.set(collection(uid).document(log.id()), toMap(log));
                }

                batch.commit().get();
            }

        } catch (Exception e) {
            throw new RuntimeException("Error saving activity logs", e);
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
