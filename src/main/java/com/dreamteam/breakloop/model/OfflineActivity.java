package com.dreamteam.breakloop.model;

public class OfflineActivity {

    private String id;
    private String title;
    private String description;
    private String category;
    private int durationMinutes;
    private String difficulty;
    private boolean active;

    public OfflineActivity() {
    }

    public OfflineActivity(
            String id,
            String title,
            String description,
            String category,
            int durationMinutes,
            String difficulty,
            boolean active) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.durationMinutes = durationMinutes;
        this.difficulty = difficulty;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}