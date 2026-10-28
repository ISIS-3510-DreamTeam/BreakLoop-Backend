package com.dreamteam.breakloop.repository;

import com.dreamteam.breakloop.model.ActivityCategory;
import com.dreamteam.breakloop.model.DeclineReason;
import com.dreamteam.breakloop.model.InteractionOutcome;
import com.dreamteam.breakloop.model.PlaceLabel;
import com.dreamteam.breakloop.model.ShownSuggestion;
import com.dreamteam.breakloop.model.SuggestionInteraction;
import com.dreamteam.breakloop.model.SuggestionReason;
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
public class FirebaseSuggestionInteractionRepository
        implements SuggestionInteractionRepository {

    private final Firestore firestore;

    public FirebaseSuggestionInteractionRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public List<SuggestionInteraction> findAll(
            String uid, Long since, Integer limit) {
        try {
            Query query = collection(uid)
                    .orderBy("openedAt", Query.Direction.DESCENDING);

            if (since != null) {
                query = query.whereGreaterThanOrEqualTo("openedAt", since);
            }

            if (limit != null) {
                query = query.limit(limit);
            }

            List<SuggestionInteraction> result = new ArrayList<>();

            for (var document : query.get().get().getDocuments()) {
                result.add(fromDocument(document));
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error retrieving suggestion interactions", e);
        }
    }

    @Override
    public Optional<SuggestionInteraction> findById(String uid, String id) {
        try {
            DocumentSnapshot document =
                    collection(uid).document(id).get().get();

            return document.exists()
                    ? Optional.of(fromDocument(document))
                    : Optional.empty();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error retrieving suggestion interaction", e);
        }
    }

    @Override
    public Optional<SuggestionInteraction> upsert(
            String uid, SuggestionInteraction incoming,
            BiFunction<SuggestionInteraction, SuggestionInteraction, Optional<SuggestionInteraction>> resolver) {
        try {
            return firestore.runTransaction(tx -> {
                DocumentReference ref =
                        collection(uid).document(incoming.id());
                DocumentSnapshot snapshot = tx.get(ref).get();

                SuggestionInteraction toStore = incoming;

                if (snapshot.exists()) {
                    Optional<SuggestionInteraction> resolved =
                            resolver.apply(fromDocument(snapshot), incoming);

                    if (resolved.isEmpty()) {
                        return Optional.<SuggestionInteraction>empty();
                    }

                    toStore = resolved.get();
                }

                tx.set(ref, toMap(toStore));

                return Optional.of(toStore);
            }).get();

        } catch (Exception e) {
            throw new RuntimeException("Error saving interaction", e);
        }
    }

    @Override
    public List<String> upsertAll(
            String uid, List<SuggestionInteraction> incoming,
            BiFunction<SuggestionInteraction, SuggestionInteraction, Optional<SuggestionInteraction>> resolver) {
        try {
            List<String> skipped = new ArrayList<>();

            for (int i = 0; i < incoming.size(); i += BATCH_SIZE) {
                List<SuggestionInteraction> chunk = incoming.subList(
                        i, Math.min(i + BATCH_SIZE, incoming.size()));

                skipped.addAll(firestore.runTransaction(tx -> {
                    DocumentReference[] refs = chunk.stream()
                            .map(SuggestionInteraction::id)
                            .distinct()
                            .map(collection(uid)::document)
                            .toArray(DocumentReference[]::new);

                    Map<String, SuggestionInteraction> stored = new HashMap<>();

                    for (DocumentSnapshot snapshot : tx.getAll(refs).get()) {
                        if (snapshot.exists()) {
                            stored.put(snapshot.getId(), fromDocument(snapshot));
                        }
                    }

                    UpsertPlanner.Plan<SuggestionInteraction> plan = UpsertPlanner.plan(
                            stored, chunk, SuggestionInteraction::id, resolver);

                    for (SuggestionInteraction item : plan.toWrite()) {
                        tx.set(collection(uid).document(item.id()), toMap(item));
                    }

                    return plan.skipped();
                }).get());
            }

            return skipped;

        } catch (Exception e) {
            throw new RuntimeException("Error saving interactions", e);
        }
    }

    private CollectionReference collection(String uid) {
        return firestore
                .collection("users")
                .document(uid)
                .collection("suggestionInteractions");
    }

    private Map<String, Object> toMap(SuggestionInteraction i) {
        Map<String, Object> map = new LinkedHashMap<>();

        put(map, "id", i.id());
        put(map, "openedAt", i.openedAt());
        put(map, "closedAt", i.closedAt());
        put(map, "timeOfDay", i.timeOfDay());
        put(map, "isWeekend", i.isWeekend());
        put(map, "weather", i.weather());
        put(map, "placeLabel", i.placeLabel());
        put(map, "latitude", i.latitude());
        put(map, "longitude", i.longitude());
        put(map, "shownSuggestions", i.shownSuggestions().stream()
                .map(this::suggestionToMap)
                .toList());
        put(map, "outcome", i.outcome());
        put(map, "acceptedActivityId", i.acceptedActivityId());
        put(map, "chosenActivityId", i.chosenActivityId());
        put(map, "declineReason", i.declineReason());

        return map;
    }

    private Map<String, Object> suggestionToMap(ShownSuggestion s) {
        Map<String, Object> map = new LinkedHashMap<>();

        put(map, "activityId", s.activityId());
        put(map, "category", s.category());
        put(map, "availableMin", s.availableMin());
        put(map, "score", s.score());
        put(map, "reasons", s.reasons().stream()
                .map(Enum::name)
                .toList());
        put(map, "shownAt", s.shownAt());

        return map;
    }

    @SuppressWarnings("unchecked")
    private SuggestionInteraction fromDocument(DocumentSnapshot d) {
        List<Map<String, Object>> shown =
                (List<Map<String, Object>>) d.get("shownSuggestions");

        return new SuggestionInteraction(
                d.getId(),
                d.getLong("openedAt"),
                d.getLong("closedAt"),
                getEnum(d, "timeOfDay", TimeOfDay.class),
                d.getBoolean("isWeekend"),
                getEnum(d, "weather", Weather.class),
                getEnum(d, "placeLabel", PlaceLabel.class),
                d.getDouble("latitude"),
                d.getDouble("longitude"),
                shown == null
                        ? List.of()
                        : shown.stream().map(this::suggestionFromMap).toList(),
                getEnum(d, "outcome", InteractionOutcome.class),
                d.getString("acceptedActivityId"),
                d.getString("chosenActivityId"),
                getEnum(d, "declineReason", DeclineReason.class)
        );
    }

    @SuppressWarnings("unchecked")
    private ShownSuggestion suggestionFromMap(Map<String, Object> m) {
        List<String> reasons = (List<String>) m.get("reasons");

        return new ShownSuggestion(
                (String) m.get("activityId"),
                ActivityCategory.valueOf((String) m.get("category")),
                ((Number) m.get("availableMin")).intValue(),
                ((Number) m.get("score")).doubleValue(),
                reasons == null
                        ? List.of()
                        : reasons.stream().map(SuggestionReason::valueOf).toList(),
                ((Number) m.get("shownAt")).longValue()
        );
    }
}
