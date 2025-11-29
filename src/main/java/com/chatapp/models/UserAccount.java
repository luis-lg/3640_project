package com.chatapp.models;

/**
 * This is separate from the WebSocket presence User model.
 */
public class UserAccount {

  private String username;
  private String password;

  public UserAccount() {
    // Default constructor needed for JSON (Jackson) deserialization
  }

  public UserAccount(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
