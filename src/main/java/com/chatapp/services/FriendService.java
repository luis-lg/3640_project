package com.chatapp.services;

import com.chatapp.models.Friend;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages friendships between users and stores them in friends.json.
 */
@Service
public class FriendService {

  private static final Logger logger = LoggerFactory.getLogger(FriendService.class);

  private static final Path FRIENDS_FILE = Paths.get("friends.json");

  private final ObjectMapper objectMapper;

  private final Set<Friend> friendships = new HashSet<>();

  public FriendService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @PostConstruct
  private void loadFromFile() {
    if (!Files.exists(FRIENDS_FILE)) {
      logger.info("friends.json not found, starting with empty friend list");
      return;
    }

    try {
      byte[] bytes = Files.readAllBytes(FRIENDS_FILE);
      if (bytes.length == 0) {
        logger.info("friends.json is empty, starting with empty friend list");
        return;
      }

      List<Friend> loaded = objectMapper.readValue(
          bytes,
          new TypeReference<List<Friend>>() {
          });
      if (loaded != null) {
        friendships.clear();
        friendships.addAll(loaded);
        logger.info("Loaded {} friendships from friends.json", friendships.size());
      }
    } catch (IOException e) {
      logger.error("Failed to read friends.json: {}", e.getMessage());
    }
  }

  /**
   * Add a friendship between two users.
   *
   * @return true if a new friendship was created, false if it already existed
   *         or input invalid.
   */
  public synchronized boolean addFriendship(String userA, String userB) {
    if (userA == null || userB == null) {
      return false;
    }
    String a = userA.trim().toLowerCase();
    String b = userB.trim().toLowerCase();

    if (a.isEmpty() || b.isEmpty() || a.equals(b)) {
      return false;
    }

    Friend friendship = new Friend(a, b);
    boolean added = friendships.add(friendship);
    if (added) {
      saveToFile();
      logger.info("Created friendship between {} and {}", a, b);
    }
    return added;
  }

  /**
   * Get list of friends for a given user.
   */
  public synchronized List<String> getFriends(String username) {
    List<String> result = new ArrayList<>();
    if (username == null) {
      return result;
    }
    String u = username.trim().toLowerCase();

    for (Friend f : friendships) {
      if (f.involves(u)) {
        String other = f.getOther(u);
        if (other != null && !result.contains(other)) {
          result.add(other);
        }
      }
    }
    result.sort(String::compareTo);
    return result;
  }

  /**
   * Check whether two users are already friends.
   */
  public synchronized boolean areFriends(String userA, String userB) {
    if (userA == null || userB == null) {
      return false;
    }
    Friend friendship = new Friend(userA, userB);
    return friendships.contains(friendship);
  }

  /**
   * Return a stable chatroom ID for a pair of users, e.g. "alice_bob".
   */
  public String getChatroomId(String userA, String userB) {
    if (userA == null || userB == null) {
      return null;
    }
    String a = userA.trim().toLowerCase();
    String b = userB.trim().toLowerCase();
    if (a.compareTo(b) <= 0) {
      return a + "_" + b;
    } else {
      return b + "_" + a;
    }
  }

  private void saveToFile() {
    try {
      byte[] json = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsBytes(new ArrayList<>(friendships));
      Files.write(FRIENDS_FILE, json);
    } catch (IOException e) {
      logger.error("Failed to write friends.json: {}", e.getMessage());
    }
  }
}
