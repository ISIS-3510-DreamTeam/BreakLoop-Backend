package com.dreamteam.breakloop.service;

import com.dreamteam.breakloop.exception.ConflictException;
import com.dreamteam.breakloop.exception.NotFoundException;
import com.dreamteam.breakloop.exception.RequestValidationException;
import com.dreamteam.breakloop.model.BatchResult;
import com.dreamteam.breakloop.model.DeclineReason;
import com.dreamteam.breakloop.model.InteractionOutcome;
import com.dreamteam.breakloop.model.ShownSuggestion;
import com.dreamteam.breakloop.model.SuggestionInteraction;
import com.dreamteam.breakloop.repository.SuggestionInteractionRepository;
import com.dreamteam.breakloop.util.Coordinates;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

@Service
public class SuggestionInteractionService {

    /** Rejects (empty) a write that would reopen a closed interaction. */
    private static final BiFunction<SuggestionInteraction,
            SuggestionInteraction, Optional<SuggestionInteraction>>
            KEEP_CLOSED = (existing, incoming) ->
                    reopens(existing, incoming)
                            ? Optional.empty()
                            : Optional.of(incoming);

    private final SuggestionInteractionRepository repository;
    private final RequestValidator validator;

    public SuggestionInteractionService(
            SuggestionInteractionRepository repository,
            RequestValidator validator
    ) {
        this.repository = repository;
        this.validator = validator;
    }

    public List<SuggestionInteraction> getInteractions(
            String uid, Long since, Integer limit) {
        return repository.findAll(uid, since, limit);
    }

    public SuggestionInteraction getInteraction(String uid, String id) {
        return repository
                .findById(uid, id)
                .orElseThrow(() -> new NotFoundException(
                        "Suggestion interaction not found"));
    }

    /**
     * Idempotent: the same body twice leaves one identical document. The
     * "closed never goes back to PENDING" check runs inside the repository's
     * transaction, so two concurrent retries cannot slip past it.
     */
    public SuggestionInteraction upsert(
            String uid, String id, SuggestionInteraction body) {

        return repository
                .upsert(uid, normalize(id, body), KEEP_CLOSED)
                .orElseThrow(() -> new ConflictException(
                        "Interaction is already closed and cannot go back to PENDING"));
    }

    /**
     * Validates every item before writing anything. Items that would reopen
     * a closed interaction are skipped instead of failing the whole batch,
     * so one stale retry cannot block the client's queue.
     */
    public BatchResult upsertAll(
            String uid, List<SuggestionInteraction> body) {

        if (body == null || body.isEmpty()) {
            throw new RequestValidationException("Body must not be empty");
        }

        List<SuggestionInteraction> normalized = new ArrayList<>();

        for (SuggestionInteraction item : body) {
            if (item == null || item.id() == null || item.id().isBlank()) {
                throw new RequestValidationException(
                        "Every item needs an id");
            }
            normalized.add(normalize(item.id(), item));
        }

        List<String> skipped =
                repository.upsertAll(uid, normalized, KEEP_CLOSED);

        return new BatchResult(normalized.size() - skipped.size(), skipped);
    }

    /**
     * Validates the body, applies the route id, default decline reasons and
     * coordinate rounding. Returns a new object; the input is untouched.
     */
    SuggestionInteraction normalize(String id, SuggestionInteraction in) {

        validator.validate(in);

        List<ShownSuggestion> shown = in.shownSuggestions() == null
                ? List.of()
                : in.shownSuggestions().stream()
                        .map(s -> new ShownSuggestion(
                                s.activityId(),
                                s.category(),
                                s.availableMin(),
                                s.score(),
                                s.reasons() == null ? List.of() : s.reasons(),
                                s.shownAt()))
                        .toList();

        InteractionOutcome outcome = in.outcome();

        if (outcome != InteractionOutcome.PENDING && in.closedAt() == null) {
            throw new RequestValidationException(
                    "closedAt is required when outcome is not PENDING");
        }

        if (in.closedAt() != null && in.closedAt() < in.openedAt()) {
            throw new RequestValidationException(
                    "closedAt must not be before openedAt");
        }

        if (outcome == InteractionOutcome.ACCEPTED) {
            String accepted = in.acceptedActivityId();

            if (accepted == null || accepted.isBlank() ||
                    shown.stream().noneMatch(
                            s -> s.activityId().equals(accepted))) {
                throw new RequestValidationException(
                        "acceptedActivityId must be one of shownSuggestions");
            }
        }

        if (outcome == InteractionOutcome.OTHER_ACTIVITY &&
                (in.chosenActivityId() == null ||
                        in.chosenActivityId().isBlank())) {
            throw new RequestValidationException(
                    "chosenActivityId is required for OTHER_ACTIVITY");
        }

        return new SuggestionInteraction(
                id,
                in.openedAt(),
                in.closedAt(),
                in.timeOfDay(),
                in.isWeekend(),
                in.weather(),
                in.placeLabel(),
                Coordinates.round(in.latitude()),
                Coordinates.round(in.longitude()),
                shown,
                outcome,
                in.acceptedActivityId(),
                in.chosenActivityId(),
                declineReason(outcome, in.declineReason())
        );
    }

    /** A decline reason only makes sense when the user did not accept. */
    private static DeclineReason declineReason(
            InteractionOutcome outcome, DeclineReason given) {

        return switch (outcome) {
            case OTHER_ACTIVITY ->
                    given != null ? given : DeclineReason.PREFERRED_OTHER;
            case NO_ACTIVITY ->
                    given != null ? given : DeclineReason.UNKNOWN;
            case PENDING, ACCEPTED -> null;
        };
    }

    static boolean reopens(
            SuggestionInteraction existing, SuggestionInteraction incoming) {

        return existing.outcome() != InteractionOutcome.PENDING &&
                incoming.outcome() == InteractionOutcome.PENDING;
    }
}
