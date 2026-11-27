package com.dreamteam.breakloop.model;

/**
 * Streak data of a user, stored in users/{uid}/progress/current.
 * Pet state lives in users/{uid} (see PetState). XP is per activity, not stored here.
 */
public class Progress {

    private int streak;
    private int longestStreak;

    public Progress() {}

    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }

    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }
}