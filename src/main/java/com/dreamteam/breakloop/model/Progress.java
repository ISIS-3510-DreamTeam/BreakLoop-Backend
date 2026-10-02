package com.dreamteam.breakloop.model;

public class Progress {

    private int xp;
    private int streak;
    private int petLevel;
    private int petHealth;

    public Progress() {}

    public Progress(int xp, int streak, int petLevel, int petHealth) {
        this.xp = xp;
        this.streak = streak;
        this.petLevel = petLevel;
        this.petHealth = petHealth;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public int getPetLevel() {
        return petLevel;
    }

    public void setPetLevel(int petLevel) {
        this.petLevel = petLevel;
    }

    public int getPetHealth() {
        return petHealth;
    }

    public void setPetHealth(int petHealth) {
        this.petHealth = petHealth;
    }
}