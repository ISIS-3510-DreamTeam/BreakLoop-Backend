package com.dreamteam.breakloop.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * One visit to the Offline screen. Times are epoch milliseconds; latitude and
 * longitude are rounded to 2 decimals by the server before being stored.
 */
public record SuggestionInteraction(
        String id,
        @NotNull @Positive Long openedAt,
        Long closedAt,
        @NotNull TimeOfDay timeOfDay,
        @JsonProperty("isWeekend") @NotNull Boolean isWeekend,
        Weather weather,
        PlaceLabel placeLabel,
        @DecimalMin("-90") @DecimalMax("90") Double latitude,
        @DecimalMin("-180") @DecimalMax("180") Double longitude,
        @Valid List<ShownSuggestion> shownSuggestions,
        @NotNull InteractionOutcome outcome,
        String acceptedActivityId,
        String chosenActivityId,
        DeclineReason declineReason
) {
}
