package com.dreamteam.breakloop.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** A suggestion card shown during one visit to the Offline screen. */
public record ShownSuggestion(
        @NotBlank String activityId,
        @NotNull ActivityCategory category,
        @Min(0) int availableMin,
        double score,
        List<SuggestionReason> reasons,
        @Min(0) long shownAt
) {
}
