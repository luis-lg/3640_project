package com.chatapp.controllers;

import com.chatapp.models.Message;
import com.chatapp.services.MessageLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST endpoint to fetch recent message history for a chatroom.
 */
@RestController
@RequestMapping("/api/messages")
public class MessageHistoryController {

  private final MessageLogService messageLogService;

  public MessageHistoryController(MessageLogService messageLogService) {
    this.messageLogService = messageLogService;
  }

  @GetMapping("/history")
  public ResponseEntity<Map<String, Object>> getHistory(
      @RequestParam("chatroomId") String chatroomId,
      @RequestParam(name = "limit", defaultValue = "50") int limit) {

    List<Message> messages = messageLogService.loadRecent(chatroomId, limit);

    Map<String, Object> body = new HashMap<>();
    body.put("chatroomId", chatroomId);
    body.put("count", messages.size());
    body.put("messages", messages);

    return ResponseEntity.ok(body);
  }
}
