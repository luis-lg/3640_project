package com.chatapp.services;

import com.chatapp.models.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Persists chat messages to JSON files, one file per chatroomId.
 * Example filenames:
 * - messages_public.json
 * - messages_alice_bob.json
 */
@Service
public class MessageLogService {

  private static final Logger logger = LoggerFactory.getLogger(MessageLogService.class);

  private final ObjectMapper objectMapper;

  public MessageLogService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  private Path getFileForChatroom(String chatroomId) {
    String id = (chatroomId == null || chatroomId.isBlank()) ? "public" : chatroomId;
    return Paths.get("messages_" + id + ".json");
  }

  /**
   * Append a message to the JSON log for its chatroom.
   */
  public synchronized void append(Message message) {
    String chatroomId = message.getChatroomId();
    Path file = getFileForChatroom(chatroomId);
    List<Message> messages = loadAllInternal(file);
    messages.add(message);
    saveAllInternal(file, messages);
  }

  /**
   * Load the most recent N messages for a chatroom.
   */
  public synchronized List<Message> loadRecent(String chatroomId, int limit) {
    Path file = getFileForChatroom(chatroomId);
    List<Message> all = loadAllInternal(file);
    if (all.isEmpty() || limit <= 0) {
      return Collections.emptyList();
    }
    int fromIndex = Math.max(0, all.size() - limit);
    return new ArrayList<>(all.subList(fromIndex, all.size()));
  }

  private List<Message> loadAllInternal(Path file) {
    if (!Files.exists(file)) {
      return new ArrayList<>();
    }
    try {
      byte[] bytes = Files.readAllBytes(file);
      if (bytes.length == 0) {
        return new ArrayList<>();
      }
      List<Message> loaded = objectMapper.readValue(
          bytes,
          new TypeReference<List<Message>>() {
          });
      return (loaded != null) ? loaded : new ArrayList<>();
    } catch (IOException e) {
      logger.error("Failed to read message log {}: {}", file, e.getMessage());
      return new ArrayList<>();
    }
  }

  private void saveAllInternal(Path file, List<Message> messages) {
    try {
      byte[] json = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsBytes(messages);
      Files.write(file, json);
    } catch (IOException e) {
      logger.error("Failed to write message log {}: {}", file, e.getMessage());
    }
  }
}
