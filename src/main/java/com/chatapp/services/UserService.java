package com.chatapp.services;

import com.chatapp.models.UserAccount;
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
import java.util.List;

/**
 * Manages user accounts and stores them in a JSON file (users.json).
 * For this class project we use plain-text passwords to keep things simple.
 */
@Service
public class UserService {

  private static final Logger logger = LoggerFactory.getLogger(UserService.class);

  private static final Path USERS_FILE = Paths.get("users.json");

  private final ObjectMapper objectMapper;

  private final List<UserAccount> accounts = new ArrayList<>();

  public UserService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @PostConstruct
  private void loadFromFile() {
    if (!Files.exists(USERS_FILE)) {
      logger.info("users.json not found, starting with empty user list");
      return;
    }

    try {
      byte[] bytes = Files.readAllBytes(USERS_FILE);
      if (bytes.length == 0) {
        logger.info("users.json is empty, starting with empty user list");
        return;
      }

      List<UserAccount> loaded = objectMapper.readValue(
          bytes,
          new TypeReference<List<UserAccount>>() {
          });
      if (loaded != null) {
        accounts.clear();
        accounts.addAll(loaded);
        logger.info("Loaded {} user accounts from users.json", accounts.size());
      }
    } catch (IOException e) {
      logger.error("Failed to read users.json: {}", e.getMessage());
    }
  }

  /**
   * Register a new user.
   *
   * @return true if the user was created, false if username is invalid or already
   *         exists.
   */
  public synchronized boolean register(UserAccount account) {
    if (account == null) {
      return false;
    }
    String username = normalize(account.getUsername());
    String password = account.getPassword();

    if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
      return false;
    }

    // check if username already exists
    if (findByUsername(username) != null) {
      return false;
    }

    // store normalized username, original password (plain text for now)
    UserAccount toStore = new UserAccount(username, password);
    accounts.add(toStore);
    saveToFile();
    logger.info("Registered new user: {}", username);
    return true;
  }

  /**
   * Check username + password.
   */
  public synchronized boolean login(UserAccount account) {
    if (account == null) {
      return false;
    }
    String username = normalize(account.getUsername());
    String password = account.getPassword();

    if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
      return false;
    }

    UserAccount existing = findByUsername(username);
    if (existing == null) {
      return false;
    }

    return password.equals(existing.getPassword());
  }

  /**
   * Helper: find user by normalized username.
   */
  private UserAccount findByUsername(String normalizedUsername) {
    if (normalizedUsername == null) {
      return null;
    }
    for (UserAccount acc : accounts) {
      if (normalizedUsername.equals(acc.getUsername())) {
        return acc;
      }
    }
    return null;
  }

  private String normalize(String username) {
    if (username == null) {
      return null;
    }
    return username.trim().toLowerCase();
  }

  /**
   * Save current list of accounts back to users.json.
   */
  private void saveToFile() {
    try {
      byte[] json = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsBytes(accounts);
      Files.write(USERS_FILE, json);
    } catch (IOException e) {
      logger.error("Failed to write users.json: {}", e.getMessage());
    }
  }
}
