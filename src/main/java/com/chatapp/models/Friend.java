package com.chatapp.models;

import java.util.Objects;

/**
 * Represents a friendship between two users.
 * Usernames are stored in normalized, sorted order so that
 * Friend("alice","bob") equals Friend("bob","alice").
 */
public class Friend {

  private String user1;
  private String user2;

  public Friend() {
    // default constructor for JSON deserialization
  }

  public Friend(String usernameA, String usernameB) {
    if (usernameA == null || usernameB == null) {
      throw new IllegalArgumentException("Usernames cannot be null");
    }
    String a = usernameA.trim().toLowerCase();
    String b = usernameB.trim().toLowerCase();

    if (a.compareTo(b) <= 0) {
      this.user1 = a;
      this.user2 = b;
    } else {
      this.user1 = b;
      this.user2 = a;
    }
  }

  public String getUser1() {
    return user1;
  }

  public void setUser1(String user1) {
    this.user1 = user1;
  }

  public String getUser2() {
    return user2;
  }

  public void setUser2(String user2) {
    this.user2 = user2;
  }

  /**
   * Return true if this friendship involves the given username.
   */
  public boolean involves(String username) {
    if (username == null)
      return false;
    String u = username.trim().toLowerCase();
    return u.equals(user1) || u.equals(user2);
  }

  /**
   * Given one username from this friendship, return the other.
   */
  public String getOther(String username) {
    if (username == null)
      return null;
    String u = username.trim().toLowerCase();
    if (u.equals(user1))
      return user2;
    if (u.equals(user2))
      return user1;
    return null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Friend friend = (Friend) o;
    return Objects.equals(user1, friend.user1) &&
        Objects.equals(user2, friend.user2);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user1, user2);
  }
}
