package com.dreamteam.breakloop.model;

import jakarta.validation.constraints.Positive;

public class AchievementUnlockRequest {

    /** Unlock time in epoch milliseconds, taken by the app when it unlocked the badge. */
    @Positive
    private long unlockedAt;

    public AchievementUnlockRequest() {}

    public long getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(long unlockedAt) { this.unlockedAt = unlockedAt; }
}