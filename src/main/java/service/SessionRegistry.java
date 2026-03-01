package service;

import model.Role;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SessionRegistry - composant principal de l'application.
 */
public class SessionRegistry {
    private final ConcurrentHashMap<String, ClientSession> sessions = new ConcurrentHashMap<>();

    public boolean isOnline(String username) {
        return sessions.containsKey(username);
    }

    public boolean register(String username, Role role, PrintWriter writer) {
        return sessions.putIfAbsent(username, new ClientSession(username, role, writer)) == null;
    }

    public void unregister(String username) {
        sessions.remove(username);
    }

    public Optional<ClientSession> sessionOf(String username) {
        return Optional.ofNullable(sessions.get(username));
    }

    public Collection<ClientSession> allSessions() {
        return sessions.values();
    }
}

