// used by controller to add and remove users and to broadcast the list of online users.

package com.chatapp.services;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UsersList {

    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    //call when user connects
    public void userConnect(String username){
        if (username != null && !username.isBlank()){
            onlineUsers.add(username);
        }
    }
    //call when user disconnects
    public void userDisconnect(String username){
        if(username != null && !username.isBlank()){
            onlineUsers.remove(username);
        }
    }

    //return online users
    public List<String> getOnlineUsers(){
        return new ArrayList<>(onlineUsers);
    }


}
