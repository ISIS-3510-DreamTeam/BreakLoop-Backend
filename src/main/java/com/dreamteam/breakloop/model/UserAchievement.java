package com.dreamteam.breakloop.model;

public class UserAchievement {

    private String id;
    private long unlockedAt;

    public UserAchievement() {}

    public UserAchievement(String id, long unlockedAt) {
        this.id = id;
        this.unlockedAt = unlockedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(long unlockedAt) { this.unlockedAt = unlockedAt; }
}