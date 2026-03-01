package service;

import model.Role;

import java.io.PrintWriter;

/**
 * ClientSession - composant principal de l'application.
 */
public record ClientSession(String username, Role role, PrintWriter writer) {
    public synchronized void sendLine(String line) {
        writer.println(line);
    }
}

