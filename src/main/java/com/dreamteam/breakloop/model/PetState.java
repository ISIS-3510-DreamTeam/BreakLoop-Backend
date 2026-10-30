package com.dreamteam.breakloop.model;

/** The pet a user has chosen and how it currently feels. */
public class PetState {

    private String activePetId;   // e.g. "sprout"
    private String petMood;       // "HAPPY" or "SAD"
    private long petUpdatedAt;    // millis, set by the phone when the change happened

    public PetState() {}

    public String getActivePetId() { return activePetId; }
    public void setActivePetId(String activePetId) { this.activePetId = activePetId; }

    public String getPetMood() { return petMood; }
    public void setPetMood(String petMood) { this.petMood = petMood; }

    public long getPetUpdatedAt() { return petUpdatedAt; }
    public void setPetUpdatedAt(long petUpdatedAt) { this.petUpdatedAt = petUpdatedAt; }
}