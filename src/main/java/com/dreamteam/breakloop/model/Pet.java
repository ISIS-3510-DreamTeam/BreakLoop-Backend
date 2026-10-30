package com.dreamteam.breakloop.model;

import java.util.Map;

/**
 * One pet type from the catalog.
 * images maps a mood name ("HAPPY", "SAD") to the path of its image,
 * relative to the server root (for example "pets/sprout_happy.png").
 */
public class Pet {

    private String id;
    private String name;
    private Map<String, String> images;

    public Pet() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Map<String, String> getImages() { return images; }
    public void setImages(Map<String, String> images) { this.images = images; }
}