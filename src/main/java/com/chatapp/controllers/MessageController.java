package com.chatapp.controllers;

import com.chatapp.models.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class MessageController {

    //clients will send message to /app/message
    @MessageMapping("/message")
    //server broadcasts message to /topic/messages
    @SendTo("/topic/messages")
    public Message handleMessage(@Payload Message userMessage) {
        return userMessage;
    }
}
