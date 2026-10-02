package com.dreamteam.breakloop.model;

public class Goal {

    private String id;
    private int targetMinutes;
    private String startDate;
    private String endDate;
    private boolean achieved;

    public Goal() {
    }

    public Goal(
            String id,
            int targetMinutes,
            String startDate,
            String endDate,
            boolean achieved) {
        this.id = id;
        this.targetMinutes = targetMinutes;
        this.startDate = startDate;
        this.endDate = endDate;
        this.achieved = achieved;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTargetMinutes() {
        return targetMinutes;
    }

    public void setTargetMinutes(int targetMinutes) {
        this.targetMinutes = targetMinutes;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public boolean isAchieved() {
        return achieved;
    }

    public void setAchieved(boolean achieved) {
        this.achieved = achieved;
    }
}