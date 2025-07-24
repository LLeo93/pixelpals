package com.pixelpals.backend.repository;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
@Component
public class OnlineUserRegistry {
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();
    public void userConnected(String username) { onlineUsers.add(username); }
    public void userDisconnected(String username) { onlineUsers.remove(username); }
    public Set<String> getOnlineUsers() { return onlineUsers; }
}
