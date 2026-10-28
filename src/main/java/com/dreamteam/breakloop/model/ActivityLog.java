package com.dreamteam.breakloop.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * An activity the user started. Times are epoch milliseconds,
 * {@code availableMin} is minutes; coordinates are rounded to 2 decimals by
 * the server before being stored.
 */
public record ActivityLog(
        String id,
        @NotBlank String activityId,
        @NotNull ActivityCategory category,
        @NotNull LogSource source,
        String interactionId,
        @NotNull @Positive Long startedAt,
        Long completedAt,
        @NotNull @Min(0) Integer xp,
        @NotNull TimeOfDay timeOfDay,
        Weather weather,
        @NotNull @Min(0) Integer availableMin,
        PlaceLabel placeLabel,
        @DecimalMin("-90") @DecimalMax("90") Double latitude,
        @DecimalMin("-180") @DecimalMax("180") Double longitude
) {
}
