package com.chatapp.controllers;

import com.chatapp.services.FriendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST endpoints for managing friendships.
 */
@RestController
@RequestMapping("/api/friends")
public class FriendController {

  private static final Logger logger = LoggerFactory.getLogger(FriendController.class);

  private final FriendService friendService;

  public FriendController(FriendService friendService) {
    this.friendService = friendService;
  }

  /**
   * Add a friendship between two users.
   *
   * Expected JSON body:
   * { "userA": "alice", "userB": "bob" }
   */
  @PostMapping("/add")
  public ResponseEntity<Map<String, Object>> addFriend(@RequestBody Map<String, String> payload) {
    String userA = payload.get("userA");
    String userB = payload.get("userB");

    Map<String, Object> body = new HashMap<>();

    boolean created = friendService.addFriendship(userA, userB);
    if (created) {
      logger.info("Created friendship between {} and {}", userA, userB);
      body.put("success", true);
      body.put("message", "Friendship created");
    } else {
      body.put("success", false);
      body.put("message", "Friendship already exists or input invalid");
    }

    // Always 200 OK for simplicity; success flag indicates result
    return ResponseEntity.ok(body);
  }

  /**
   * Get all friends of a user.
   *
   * Example: GET /api/friends/list?username=alice
   */
  @GetMapping("/list")
  public ResponseEntity<Map<String, Object>> listFriends(@RequestParam("username") String username) {
    List<String> friends = friendService.getFriends(username);

    Map<String, Object> body = new HashMap<>();
    body.put("username", username);
    body.put("friends", friends);
    body.put("count", friends.size());

    return ResponseEntity.ok(body);
  }

  /**
   * Utility endpoint to get the chatroomId for two users.
   *
   * Example: GET /api/friends/chatroom-id?userA=alice&userB=bob
   */
  @GetMapping("/chatroom-id")
  public ResponseEntity<Map<String, String>> getChatroomId(@RequestParam("userA") String userA,
      @RequestParam("userB") String userB) {
    String id = friendService.getChatroomId(userA, userB);
    Map<String, String> body = new HashMap<>();
    body.put("chatroomId", id);
    return ResponseEntity.ok(body);
  }
}
