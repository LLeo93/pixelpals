package com.pixelpals.backend.WebSocket;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Set;
import java.util.Collections;
@Component
public class UserSessionRegistry {
    private final ConcurrentMap<String, Set<String>> activeUserSessions = new ConcurrentHashMap<>();
    public boolean registerSession(String userId, String sessionId) {
        Set<String> sessions = activeUserSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());
        boolean wasOffline = sessions.isEmpty();
        sessions.add(sessionId);
        return wasOffline;
    }
    public boolean deregisterSession(String userId, String sessionId) {
        Set<String> sessions = activeUserSessions.get(userId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                activeUserSessions.remove(userId);
                return true;
            }
        }
        return false;
    }
    public boolean isUserOnline(String userId) {
        return activeUserSessions.containsKey(userId) && !activeUserSessions.get(userId).isEmpty();
    }
    public Set<String> getOnlineUserIds() {
        return Collections.unmodifiableSet(activeUserSessions.keySet());
    }
}
