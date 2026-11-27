package com.dreamteam.breakloop.model;

import jakarta.validation.constraints.Min;

/**
 * Body of PUT /api/users/{uid}/progress. Both fields are optional:
 * only the ones that arrive are written.
 */
public class ProgressUpdate {

    @Min(0)
    private Integer streak;

    @Min(0)
    private Integer longestStreak;

    public Integer getStreak() { return streak; }
    public void setStreak(Integer streak) { this.streak = streak; }

    public Integer getLongestStreak() { return longestStreak; }
    public void setLongestStreak(Integer longestStreak) { this.longestStreak = longestStreak; }
}