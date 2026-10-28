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
import com.dreamteam.breakloop.repository.SuggestionInteractionRepository;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SuggestionInteractionServiceTest {

    private final SuggestionInteractionRepository repository =
            mock(SuggestionInteractionRepository.class);

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
        SuggestionInteraction closed = service.normalize("x", interaction(
                InteractionOutcome.NO_ACTIVITY, 2_000L, null, null, null));
        when(repository.findById("u", "x")).thenReturn(Optional.of(closed));

        assertThrows(ConflictException.class,
                () -> service.upsert("u", "x", pending()));
        verify(repository, never()).save(any(), any());
    }

    @Test
    void pendingCanBeClosedAndClosedCanBeRewritten() {
        SuggestionInteraction open = service.normalize("x", pending());
        when(repository.findById("u", "x")).thenReturn(Optional.of(open));

        service.upsert("u", "x", interaction(
                InteractionOutcome.ACCEPTED, 2_000L, "a1", null, null));
        verify(repository).save(eq("u"), any());
    }

    @Test
    void batchSkipsReopeningItemsAndWritesTheRest() {
        SuggestionInteraction closed = service.normalize("old", interaction(
                InteractionOutcome.NO_ACTIVITY, 2_000L, null, null, null));
        when(repository.findByIds(eq("u"), anyCollection()))
                .thenReturn(Map.of("old", closed));

        BatchResult result = service.upsertAll("u", List.of(
                withId("old", pending()), withId("new", pending())));

        assertEquals(1, result.written());
        assertEquals(List.of("old"), result.skipped());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SuggestionInteraction>> saved =
                ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(eq("u"), saved.capture());
        assertEquals("new", saved.getValue().get(0).id());
    }

    @Test
    void batchDoesNotWriteAnythingIfOneItemIsInvalid() {
        SuggestionInteraction bad = withId("b", interaction(
                InteractionOutcome.ACCEPTED, 2_000L, null, null, null));

        assertThrows(RequestValidationException.class, () ->
                service.upsertAll("u", List.of(withId("a", pending()), bad)));
        verify(repository, never()).saveAll(any(), anyList());
    }

    @Test
    void batchDetectsReopeningWithinTheSameBatch() {
        when(repository.findByIds(eq("u"), anyCollection()))
                .thenReturn(Map.of());

        BatchResult result = service.upsertAll("u", List.of(
                withId("a", interaction(InteractionOutcome.NO_ACTIVITY, 2_000L,
                        null, null, null)),
                withId("a", pending())));

        assertEquals(List.of("a"), result.skipped());
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
