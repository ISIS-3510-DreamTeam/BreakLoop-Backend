package com.dreamteam.breakloop.service;

import com.dreamteam.breakloop.exception.NotFoundException;
import com.dreamteam.breakloop.exception.RequestValidationException;
import com.dreamteam.breakloop.model.ActivityLog;
import com.dreamteam.breakloop.model.BatchResult;
import com.dreamteam.breakloop.repository.ActivityLogRepository;
import com.dreamteam.breakloop.util.Coordinates;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

@Service
public class ActivityLogService {

    private static final BiFunction<ActivityLog, ActivityLog,
            Optional<ActivityLog>> KEEP_COMPLETION =
            (existing, incoming) -> Optional.of(
                    keepCompletion(existing, incoming));

    private final ActivityLogRepository repository;
    private final RequestValidator validator;

    public ActivityLogService(
            ActivityLogRepository repository,
            RequestValidator validator
    ) {
        this.repository = repository;
        this.validator = validator;
    }

    public List<ActivityLog> getLogs(String uid, Long since, Integer limit) {
        return repository.findAll(uid, since, limit);
    }

    public ActivityLog getLog(String uid, String id) {
        return repository
                .findById(uid, id)
                .orElseThrow(() -> new NotFoundException(
                        "Activity log not found"));
    }

    /**
     * Idempotent: the same body twice leaves one identical document. The
     * merge with the stored log runs inside the repository's transaction.
     */
    public ActivityLog upsert(String uid, String id, ActivityLog body) {

        return repository
                .upsert(uid, normalize(id, body), KEEP_COMPLETION)
                .orElseThrow();
    }

    public BatchResult upsertAll(String uid, List<ActivityLog> body) {

        if (body == null || body.isEmpty()) {
            throw new RequestValidationException("Body must not be empty");
        }

        List<ActivityLog> normalized = new ArrayList<>();

        for (ActivityLog item : body) {
            if (item == null || item.id() == null || item.id().isBlank()) {
                throw new RequestValidationException(
                        "Every item needs an id");
            }
            normalized.add(normalize(item.id(), item));
        }

        repository.upsertAll(uid, normalized, KEEP_COMPLETION);

        return new BatchResult(normalized.size(), List.of());
    }

    /** Applies the route id and coordinate rounding; input is untouched. */
    ActivityLog normalize(String id, ActivityLog in) {

        validator.validate(in);

        if (in.completedAt() != null && in.completedAt() < in.startedAt()) {
            throw new RequestValidationException(
                    "completedAt must not be before startedAt");
        }

        return new ActivityLog(
                id,
                in.activityId(),
                in.category(),
                in.source(),
                in.interactionId(),
                in.startedAt(),
                in.completedAt(),
                in.xp(),
                in.timeOfDay(),
                in.weather(),
                in.availableMin(),
                in.placeLabel(),
                Coordinates.round(in.latitude()),
                Coordinates.round(in.longitude())
        );
    }

    /**
     * A retried "started" write that arrives after "completed" must not
     * erase the completion.
     */
    static ActivityLog keepCompletion(ActivityLog existing, ActivityLog in) {

        if (existing.completedAt() == null ||
                in.completedAt() != null) {
            return in;
        }

        return new ActivityLog(
                in.id(),
                in.activityId(),
                in.category(),
                in.source(),
                in.interactionId(),
                in.startedAt(),
                existing.completedAt(),
                in.xp(),
                in.timeOfDay(),
                in.weather(),
                in.availableMin(),
                in.placeLabel(),
                in.latitude(),
                in.longitude()
        );
    }
}
