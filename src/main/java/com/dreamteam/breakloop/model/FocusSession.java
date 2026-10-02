package com.dreamteam.breakloop.model;

public class FocusSession {

    private String id;
    private Object startTime;
    private int duration;
    private String type;
    private String status;
    private int xpEarned;

    public FocusSession() {
    }

    public FocusSession(
            String id,
            Object startTime,
            int duration,
            String type,
            String status,
            int xpEarned) {
        this.id = id;
        this.startTime = startTime;
        this.duration = duration;
        this.type = type;
        this.status = status;
        this.xpEarned = xpEarned;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getStartTime() {
        return startTime;
    }

    public void setStartTime(Object startTime) {
        this.startTime = startTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getXpEarned() {
        return xpEarned;
    }

    public void setXpEarned(int xpEarned) {
        this.xpEarned = xpEarned;
    }
}