package com.dujo.chefsbook.data.model;

import java.util.HashMap;
import java.util.Map;

public class Recipe {
    private String id;
    private String name;
    private String description;
    private float rating;
    private String recipeCategoryId;

    public Recipe(String id, String name, String description, float rating, String recipeCategoryId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.rating = rating;
        this.recipeCategoryId = recipeCategoryId;
    }

    public Recipe() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getRecipeCategoryId() {
        return recipeCategoryId;
    }

    public void setRecipeCategoryId(String recipeCategoryId) {
        this.recipeCategoryId = recipeCategoryId;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("description", description);
        m.put("rating", rating);
        m.put("recipeCategoryId", recipeCategoryId);
        return m;
    }

    public static Recipe fromMap(String id, Map<String, Object> map) {
        if (map == null) return null;
        String name = (String) map.get("name");
        String description = (String) map.get("description");
        double ratingD = 0.0;
        Object r = map.get("rating");
        if (r instanceof Number) ratingD = ((Number) r).doubleValue();
        String category = (String) map.get("recipeCategoryId");
        return new Recipe(id, name, description, (float) ratingD, category);
    }
}
