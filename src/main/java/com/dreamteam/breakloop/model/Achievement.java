package com.dreamteam.breakloop.model;

/**
 * One achievement from the catalog.
 * image is a path relative to the server root (for example "achievements/streak_7.png"),
 * the same convention as pets. The app joins it with the base URL.
 */
public class Achievement {

    private String id;
    private String name;
    private String description;
    private String image;
    private AchievementRuleType rule;
    private int target;

    public Achievement() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public AchievementRuleType getRule() { return rule; }
    public void setRule(AchievementRuleType rule) { this.rule = rule; }

    public int getTarget() { return target; }
    public void setTarget(int target) { this.target = target; }
    
}