package com.dreamteam.breakloop.service;

import com.dreamteam.breakloop.exception.ConflictException;
import com.dreamteam.breakloop.exception.RequestValidationException;
import com.dreamteam.breakloop.model.ActivityCategory;
import com.dreamteam.breakloop.model.BatchResult;
import com.dreamteam.breakloop.model.DeclineReason;
import com.dreamteam.breakloop.model.InteractionOutcome;
import com.dreamteam.breakloop.model.ShownSuggestion;
import com.dreamteam.breakloop.model.SuggestionInteraction;
import com.dreamteam.breakloop.model.TimeOfDay;
import com.dreamteam.breakloop.repository.InMemorySuggestionInteractionRepository;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SuggestionInteractionServiceTest {

    private final InMemorySuggestionInteractionRepository repository =
            new InMemorySuggestionInteractionRepository();

    private final SuggestionInteractionService service =
            new SuggestionInteractionService(
                    repository,
                    new RequestValidator(Validation
                            .buildDefaultValidatorFactory().getValidator()));

    private static ShownSuggestion shown(String activityId) {
        return new ShownSuggestion(
                activityId, ActivityCategory.MOVE, 15, 0.8, null, 1_000L);
    }

    private static SuggestionInteraction interaction(
            InteractionOutcome outcome,
            Long closedAt,
            String accepted,
            String chosen,
            DeclineReason reason) {
        return new SuggestionInteraction(
                null, 1_000L, closedAt, TimeOfDay.MORNING, false, null, null,
                4.71234, -74.06789, List.of(shown("a1")), outcome,
                accepted, chosen, reason);
    }

    private static SuggestionInteraction pending() {
        return interaction(InteractionOutcome.PENDING, null, null, null, null);
    }

    @Test
    void roundsCoordinatesAndUsesRouteId() {
        SuggestionInteraction result =
                service.normalize("route-id", pending());

        assertEquals("route-id", result.id());
        assertEquals(4.71, result.latitude());
        assertEquals(-74.07, result.longitude());
    }

    @Test
    void nullReasonsBecomeEmptyList() {
        assertEquals(
                List.of(),
                service.normalize("x", pending())
                        .shownSuggestions().get(0).reasons());
    }

    @Test
    void otherActivityWithoutReasonDefaultsToPreferredOther() {
        SuggestionInteraction result = service.normalize("x", interaction(
                InteractionOutcome.OTHER_ACTIVITY, 2_000L, null, "a9", null));

        assertEquals(DeclineReason.PREFERRED_OTHER, result.declineReason());
    }

    @Test
    void noActivityWithoutReasonDefaultsToUnknown() {
        SuggestionInteraction result = service.normalize("x", interaction(
                InteractionOutcome.NO_ACTIVITY, 2_000L, null, null, null));

        assertEquals(DeclineReason.UNKNOWN, result.declineReason());
    }

    @Test
    void givenDeclineReasonIsKept() {
        SuggestionInteraction result = service.normalize("x", interaction(
                InteractionOutcome.NO_ACTIVITY, 2_000L, null, null,
                DeclineReason.WEATHER));

        assertEquals(DeclineReason.WEATHER, result.declineReason());
    }

    @Test
    void acceptedHasNoDeclineReason() {
        SuggestionInteraction result = service.normalize("x", interaction(
                InteractionOutcome.ACCEPTED, 2_000L, "a1", null,
                DeclineReason.WEATHER));

        assertNull(result.declineReason());
    }

    @Test
    void acceptedWithoutActivityIdIsRejected() {
        assertThrows(RequestValidationException.class, () ->
                service.normalize("x", interaction(
                        InteractionOutcome.ACCEPTED, 2_000L, null, null, null)));
    }

    @Test
    void acceptedActivityMustBeAmongShown() {
        assertThrows(RequestValidationException.class, () ->
                service.normalize("x", interaction(
                        InteractionOutcome.ACCEPTED, 2_000L, "zzz", null, null)));
    }

    @Test
    void otherActivityWithoutChosenIsRejected() {
        assertThrows(RequestValidationException.class, () ->
                service.normalize("x", interaction(
                        InteractionOutcome.OTHER_ACTIVITY, 2_000L, null, null,
                        null)));
    }

    @Test
    void closedWithoutClosedAtIsRejected() {
        assertThrows(RequestValidationException.class, () ->
                service.normalize("x", interaction(
                        InteractionOutcome.NO_ACTIVITY, null, null, null, null)));
    }

    @Test
    void closedBeforeOpenedIsRejected() {
        assertThrows(RequestValidationException.class, () ->
                service.normalize("x", interaction(
                        InteractionOutcome.NO_ACTIVITY, 500L, null, null, null)));
    }

    @Test
    void missingRequiredFieldsAreRejected() {
        SuggestionInteraction empty = new SuggestionInteraction(
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null);

        assertThrows(RequestValidationException.class,
                () -> service.normalize("x", empty));
    }

    @Test
    void closedInteractionCannotGoBackToPending() {
        service.upsert("u", "x", interaction(
                InteractionOutcome.NO_ACTIVITY, 2_000L, null, null, null));

        assertThrows(ConflictException.class,
                () -> service.upsert("u", "x", pending()));
        assertEquals(InteractionOutcome.NO_ACTIVITY,
                repository.stored.get("x").outcome());
    }

    @Test
    void pendingCanBeClosedAndClosedCanBeRewritten() {
        service.upsert("u", "x", pending());
        service.upsert("u", "x", interaction(
                InteractionOutcome.ACCEPTED, 2_000L, "a1", null, null));
        service.upsert("u", "x", interaction(
                InteractionOutcome.NO_ACTIVITY, 3_000L, null, null, null));

        assertEquals(InteractionOutcome.NO_ACTIVITY,
                repository.stored.get("x").outcome());
    }

    @Test
    void sameRequestTwiceLeavesOneIdenticalDocument() {
        SuggestionInteraction first = service.upsert("u", "x", pending());
        SuggestionInteraction second = service.upsert("u", "x", pending());

        assertEquals(first, second);
        assertEquals(1, repository.stored.size());
    }

    @Test
    void concurrentPendingRetryNeverOverwritesTheClose() throws Exception {
        // Many stale PENDING retries race with the close of the same visit
        service.upsert("u", "x", pending());

        var pool = java.util.concurrent.Executors.newFixedThreadPool(8);
        List<java.util.concurrent.Future<?>> futures = new java.util.ArrayList<>();
        for (int i = 0; i < 200; i++) {
            final boolean close = i == 100;
            futures.add(pool.submit(() -> {
                try {
                    service.upsert("u", "x", close
                            ? interaction(InteractionOutcome.NO_ACTIVITY,
                                    2_000L, null, null, null)
                            : pending());
                } catch (ConflictException expected) {
                    // retry arrived after the close
                }
            }));
        }
        for (var f : futures) {
            f.get();
        }
        pool.shutdown();

        assertEquals(InteractionOutcome.NO_ACTIVITY,
                repository.stored.get("x").outcome());
    }

    @Test
    void batchSkipsReopeningItemsAndWritesTheRest() {
        service.upsert("u", "old", interaction(
                InteractionOutcome.NO_ACTIVITY, 2_000L, null, null, null));

        BatchResult result = service.upsertAll("u", List.of(
                withId("old", pending()), withId("new", pending())));

        assertEquals(1, result.written());
        assertEquals(List.of("old"), result.skipped());
        assertEquals(InteractionOutcome.PENDING,
                repository.stored.get("new").outcome());
        assertEquals(InteractionOutcome.NO_ACTIVITY,
                repository.stored.get("old").outcome());
    }

    @Test
    void batchDoesNotWriteAnythingIfOneItemIsInvalid() {
        SuggestionInteraction bad = withId("b", interaction(
                InteractionOutcome.ACCEPTED, 2_000L, null, null, null));

        assertThrows(RequestValidationException.class, () ->
                service.upsertAll("u", List.of(withId("a", pending()), bad)));
        assertEquals(0, repository.stored.size());
    }

    @Test
    void batchDetectsReopeningWithinTheSameBatch() {
        BatchResult result = service.upsertAll("u", List.of(
                withId("a", interaction(InteractionOutcome.NO_ACTIVITY, 2_000L,
                        null, null, null)),
                withId("a", pending())));

        assertEquals(List.of("a"), result.skipped());
        assertEquals(1, result.written());
    }

    @Test
    void emptyBatchIsRejected() {
        assertThrows(RequestValidationException.class,
                () -> service.upsertAll("u", List.of()));
    }

    private static SuggestionInteraction withId(
            String id, SuggestionInteraction i) {
        return new SuggestionInteraction(
                id, i.openedAt(), i.closedAt(), i.timeOfDay(), i.isWeekend(),
                i.weather(), i.placeLabel(), i.latitude(), i.longitude(),
                i.shownSuggestions(), i.outcome(), i.acceptedActivityId(),
                i.chosenActivityId(), i.declineReason());
    }
}
