package com.dreamteam.breakloop.model;

public class Usage {

    private String date;
    private long screenTimeMs;
    private int pickups;
    private int unlocks;
    private boolean partial;
    private Object updatedAt;

    public Usage() {
    }

    public Usage(
            String date,
            long screenTimeMs,
            int pickups,
            int unlocks,
            boolean partial,
            Object updatedAt) {
        this.date = date;
        this.screenTimeMs = screenTimeMs;
        this.pickups = pickups;
        this.unlocks = unlocks;
        this.partial = partial;
        this.updatedAt = updatedAt;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getScreenTimeMs() {
        return screenTimeMs;
    }

    public void setScreenTimeMs(long screenTimeMs) {
        this.screenTimeMs = screenTimeMs;
    }

    public int getPickups() {
        return pickups;
    }

    public void setPickups(int pickups) {
        this.pickups = pickups;
    }

    public int getUnlocks() {
        return unlocks;
    }

    public void setUnlocks(int unlocks) {
        this.unlocks = unlocks;
    }

    public boolean isPartial() {
        return partial;
    }

    public void setPartial(boolean partial) {
        this.partial = partial;
    }

    public Object getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Object updatedAt) {
        this.updatedAt = updatedAt;
    }
}