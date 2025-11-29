package com.chatapp.controllers;

import com.chatapp.models.Message;
import com.chatapp.services.MessageLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.Instant;

@Controller
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);
    private final MessageLogService messageLogService;

    public MessageController(MessageLogService messageLogService) {
        this.messageLogService = messageLogService;
    }

    // clients will send message to /app/message (public chat)
    @MessageMapping("/message")
    // server broadcasts message to /topic/messages
    @SendTo("/topic/messages")
    public Message handlePublicMessage(@Payload Message userMessage) {

        // if the client didn't provide a timestamp, set it on the server
        if (userMessage.getTimestamp() == null || userMessage.getTimestamp().isEmpty()) {
            userMessage.setTimestamp(Instant.now().toString());
        }

        // log the incoming message with user + timestamp + text
        logger.info("Received message from {} at {}: {}",
                userMessage.getUser(),
                userMessage.getTimestamp(),
                userMessage.getText());

        // force chatroomId to public for clarity
        userMessage.setChatroomId("public");

        // append to persistent log
        messageLogService.append(userMessage);

        // return the message so it gets broadcast to all subscribers
        return userMessage;
    }

    // clients will send private messages to /app/private/{chatroomId}
    // server broadcasts to /topic/private/{chatroomId}
    @MessageMapping("/private/{chatroomId}")
    @SendTo("/topic/private/{chatroomId}")
    public Message handlePrivateMessage(@Payload Message userMessage) {

        if (userMessage.getTimestamp() == null || userMessage.getTimestamp().isEmpty()) {
            userMessage.setTimestamp(Instant.now().toString());
        }

        logger.info("Private message in {} from {} at {}: {}",
                userMessage.getChatroomId(),
                userMessage.getUser(),
                userMessage.getTimestamp(),
                userMessage.getText());

        // append to persistent log
        messageLogService.append(userMessage);

        return userMessage;
    }
}