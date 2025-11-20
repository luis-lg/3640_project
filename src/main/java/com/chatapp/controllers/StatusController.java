// handles users connect and disconnect messages from websocket.
package com.chatapp.controllers;

import com.chatapp.models.User;
import com.chatapp.services.UsersList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class StatusController {
    private static  Logger logger = LoggerFactory.getLogger(StatusController.class);

    private UsersList usersList;
    private SimpMessagingTemplate messagingTemplate;

    public StatusController(UsersList usersList, SimpMessagingTemplate messagingTemplate){
        this.usersList = usersList;
        this.messagingTemplate = messagingTemplate;
    }

    //handles messages sent to /app/connect
    @MessageMapping("/connect")
    public void handleConnect(@Payload User user){
        if(user == null || user.getUsername() == null){
            logger.warn("Connect payload is invalid");
            return;
        }

        //add the user to the online list
        String username = user.getUsername();
        usersList.userConnect(username);

        List<String> online = usersList.getOnlineUsers();
        logger.info("User joined: {} | online now: {}", username, online);

        // broadcast updated user list to subscribers
        messagingTemplate.convertAndSend("/topic/users", online);
    }

    //handles messages sent to /app/disconnect
    @MessageMapping("/disconnect")
    public void handleDisconnect(@Payload User user) {
        if (user == null || user.getUsername() == null) {
            logger.warn("Leave payload is invalid");
            return;
        }

        //remove user from online users list
        String username = user.getUsername();
        usersList.userDisconnect(username);

        List<String> online = usersList.getOnlineUsers();
        logger.info("User {} left | online now: {}", username, online);

        // broadcast updated user list to all subscribers
        messagingTemplate.convertAndSend("/topic/users", online);
    }
}
