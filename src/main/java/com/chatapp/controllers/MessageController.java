package com.chatapp.controllers;

import com.chatapp.models.Message;
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

    // clients will send message to /app/message
    @MessageMapping("/message")
    // server broadcasts message to /topic/messages
    @SendTo("/topic/messages")
    public Message handleMessage(@Payload Message userMessage) {

        // if the client didn't provide a timestamp, set it on the server
        if (userMessage.getTimestamp() == null || userMessage.getTimestamp().isEmpty()) {
            userMessage.setTimestamp(Instant.now().toString());
        }

        // log the incoming message with user + timestamp + text
        logger.info("Received message from {} at {}: {}",
                userMessage.getUser(),
                userMessage.getTimestamp(),
                userMessage.getText());

        // return the message so it gets broadcast to all subscribers
        return userMessage;
    }
}