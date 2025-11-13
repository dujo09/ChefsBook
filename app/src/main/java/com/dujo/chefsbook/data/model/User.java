package com.dujo.chefsbook.data.model;

public class User {
  public String uid;
  public String email;
  public String username;
  public String role;

  public User() {}

  public User(String uid, String email, String username, String role) {
    this.uid = uid;
    this.email = email;
    this.username = username;
    this.role = role;
  }
}
