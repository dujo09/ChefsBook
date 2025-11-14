package com.dujo.chefsbook.data.model;

import java.util.HashMap;
import java.util.Map;

public class Recipe {
  private String id;
  private String ownerId;
  private String recipeCategoryId;
  private String name;
  private String description;
  private float rating;
  private String imageUrl;

  public Recipe(
      String id,
      String ownerId,
      String recipeCategoryId,
      String name,
      String description,
      float rating,
      String imageUrl) {
    this.id = id;
    this.name = name;
    this.ownerId = ownerId;
    this.description = description;
    this.rating = rating;
    this.imageUrl = imageUrl;
    this.recipeCategoryId = recipeCategoryId;
  }

  public Recipe() {}

  public Recipe(Recipe other) {
    this.name = other.name;
    this.ownerId = other.ownerId;
    this.description = other.description;
    this.rating = other.rating;
    this.imageUrl = other.imageUrl;
    this.recipeCategoryId = other.recipeCategoryId;
  }

  public static Recipe fromMap(String id, Map<String, Object> map) {
    if (map == null) return null;
    String name = (String) map.get("name");
    String description = (String) map.get("description");
    double ratingD = 0.0;
    Object r = map.get("rating");
    if (r instanceof Number) ratingD = ((Number) r).doubleValue();
    String imageUrl = (String) map.get("imageUrl");
    String category = (String) map.get("recipeCategoryId");
    String ownerId = (String) map.get("ownerId");
    return new Recipe(id, ownerId, category, name, description, (float) ratingD, imageUrl);
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
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

  public String getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public Map<String, Object> toMap() {
    Map<String, Object> m = new HashMap<>();
    m.put("name", name);
    m.put("description", description);
    m.put("rating", rating);
    m.put("imageUrl", imageUrl);
    m.put("recipeCategoryId", recipeCategoryId);
    m.put("ownerId", ownerId);
    return m;
  }
}
