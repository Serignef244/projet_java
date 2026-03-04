package server.handler;

import util.Protocol;
import model.Message;
import model.Role;
import model.User;
import service.AuthService;
import service.ClientSession;
import service.MessageService;
import service.ServerServices;
import service.SessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * ClientHandler - composant principal de l'application.
 */
public class ClientHandler implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);
    private static final DateTimeFormatter TS = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final Socket socket;
    private final ServerServices services;
    private String loggedUsername;

    public ClientHandler(Socket socket, ServerServices services) {
        this.socket = socket;
        this.services = services;
    }

    @Override
    public void run() {
        try (Socket closeableSocket = socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(closeableSocket.getInputStream()));
             PrintWriter out = new PrintWriter(closeableSocket.getOutputStream(), true)) {
            out.println("INFO|CONNECTE");
            String line;
            while ((line = in.readLine()) != null) {
                handleCommand(line, out);
            }
        } catch (IOException e) {
            LOGGER.warn("Connexion interrompue: {}", e.getMessage());
        } finally {
            cleanupSession();
        }
    }

    private void handleCommand(String line, PrintWriter out) {
        List<String> tokens = Protocol.split(line);
        if (tokens.isEmpty()) {
            out.println("ERR|BAD_REQUEST|Commande vide");
            return;
        }

        String action = tokens.get(0);
        switch (action) {
            case "REGISTER" -> handleRegister(tokens, out);
            case "LOGIN" -> handleLogin(tokens, out);
            case "MSG" -> handleMessage(tokens, out);
            case "HISTORY" -> handleHistory(tokens, out);
            case "LIST_USERS" -> handleListUsers(out);
            case "LOGOUT" -> {
                out.println("OK|LOGOUT");
                cleanupSession();
            }
            default -> out.println("ERR|BAD_REQUEST|Commande inconnue");
        }
    }

    private void handleRegister(List<String> tokens, PrintWriter out) {
        // RG1/RG9: création de compte avec validation et hash côté service.
        if (tokens.size() != 4) {
            out.println("ERR|BAD_REQUEST|REGISTER attendu: REGISTER|user|passwordB64|role");
            return;
        }
        String username = tokens.get(1);
        String password = safeDecode(tokens.get(2), "mot de passe");
        if (password == null) {
            out.println("ERR|BAD_REQUEST|Password invalide (Base64)");
            return;
        }
        Role role;
        try {
            role = Role.valueOf(tokens.get(3));
        } catch (IllegalArgumentException e) {
            out.println("ERR|BAD_REQUEST|Role invalide");
            return;
        }

        Optional<User> created = services.authService().register(username, password, role);
        if (created.isEmpty()) {
            out.println("ERR|REGISTER|Nom d'utilisateur deja utilise ou donnees invalides");
            return;
        }
        LOGGER.info("Inscription reussie pour {}", username);
        out.println("OK|REGISTER|" + username);
    }

    private void handleLogin(List<String> tokens, PrintWriter out) {
        // RG2/RG3/RG4: auth requise, anti double session, passage ONLINE.
        if (tokens.size() != 3) {
            out.println("ERR|BAD_REQUEST|LOGIN attendu: LOGIN|user|passwordB64");
            return;
        }
        if (loggedUsername != null) {
            out.println("ERR|LOGIN|Session deja authentifiee");
            return;
        }

        String username = tokens.get(1);
        String password = safeDecode(tokens.get(2), "mot de passe");
        if (password == null) {
            out.println("ERR|BAD_REQUEST|Password invalide (Base64)");
            return;
        }
        AuthService authService = services.authService();
        Optional<User> user = authService.authenticate(username, password);
        if (user.isEmpty()) {
            out.println("ERR|LOGIN|Identifiants invalides");
            return;
        }
        if (!authService.canLogin(user.get())) {
            out.println("ERR|LOGIN|Utilisateur deja connecte");
            return;
        }
        SessionRegistry sessions = services.sessionRegistry();
        boolean inserted = sessions.register(username, user.get().getRole(), out);
        if (!inserted) {
            out.println("ERR|LOGIN|Utilisateur deja connecte");
            return;
        }

        authService.markOnline(username);
        loggedUsername = username;
        LOGGER.info("Connexion: {} ({})", username, user.get().getRole());

        out.println("OK|LOGIN|" + username + "|" + user.get().getRole());
        broadcastStatus(username, "ONLINE");
        deliverPendingMessages(username, out);
        handleListUsers(out);
    }

    private void handleMessage(List<String> tokens, PrintWriter out) {
        // RG2/RG5/RG6/RG7: contrôle auth, destinataire, contenu et offline queue.
        if (!isAuthenticated(out)) {
            return;
        }
        if (tokens.size() < 3) {
            out.println("ERR|BAD_REQUEST|MSG attendu: MSG|recipient|contentB64");
            return;
        }
        String recipient = tokens.get(1);
        String content = safeDecode(tokens.get(2), "message");
        if (content == null) {
            out.println("ERR|BAD_REQUEST|Message invalide (Base64)");
            return;
        }

        MessageService messageService = services.messageService();
        if (messageService.findUser(recipient).isEmpty()) {
            out.println("ERR|MSG|Destinataire introuvable");
            return;
        }

        boolean recipientOnline = services.sessionRegistry().isOnline(recipient);
        Optional<Message> messageOpt = messageService.createMessage(loggedUsername, recipient, content, recipientOnline);
        if (messageOpt.isEmpty()) {
            out.println("ERR|MSG|Message invalide (vide ou > 1000)");
            return;
        }

        Message message = messageOpt.get();
        String payload = "MESSAGE|" + loggedUsername
                + "|" + recipient
                + "|" + TS.format(message.getTimestamp())
                + "|" + Protocol.encode(message.getContent());

        services.sessionRegistry().sessionOf(loggedUsername).ifPresent(s -> s.sendLine(payload));
        services.sessionRegistry().sessionOf(recipient).ifPresent(s -> s.sendLine(payload));

        if (recipientOnline) {
            out.println("OK|MSG|DELIVERED");
            LOGGER.info("Message de {} a {} : \"{}\" (livre)", loggedUsername, recipient, sanitize(content));
        } else {
            out.println("OK|MSG|QUEUED");
            LOGGER.info("Message de {} a {} : \"{}\" (differe)", loggedUsername, recipient, sanitize(content));
        }
    }

    private void handleHistory(List<String> tokens, PrintWriter out) {
        // RG2/RG8: historique autorisé uniquement si connecté et tri chronologique.
        if (!isAuthenticated(out)) {
            return;
        }
        if (tokens.size() != 2) {
            out.println("ERR|BAD_REQUEST|HISTORY attendu: HISTORY|otherUser");
            return;
        }
        String other = tokens.get(1);
        List<Message> history = services.messageService().history(loggedUsername, other);
        out.println("OK|HISTORY|" + other);
        for (Message message : history) {
            out.println(formatHistoryLine(message));
        }
        out.println("END|HISTORY");
        services.messageService().markReadConversation(loggedUsername, other);
    }

    private void handleListUsers(PrintWriter out) {
        if (!isAuthenticated(out)) {
            return;
        }
        Optional<User> currentUser = services.userRepository().findByUsername(loggedUsername);
        if (currentUser.isEmpty()) {
            out.println("ERR|LIST_USERS|Utilisateur introuvable");
            return;
        }
        List<User> users = services.authService().listVisibleUsers(currentUser.get());
        out.println("OK|LIST_USERS");
        for (User user : users) {
            out.println("USERS_ITEM|" + user.getUsername() + "|" + user.getStatus() + "|" + user.getRole());
        }
        out.println("END|USERS");
    }

    private void deliverPendingMessages(String username, PrintWriter out) {
        List<Message> pending = services.messageService().pendingFor(username);
        for (Message message : pending) {
            out.println(formatMessageLine(message));
        }
        services.messageService().markReceived(pending);
    }

    private String formatMessageLine(Message message) {
        return "MESSAGE|" + message.getSender().getUsername()
                + "|" + message.getRecipient().getUsername()
                + "|" + TS.format(message.getTimestamp())
                + "|" + Protocol.encode(message.getContent());
    }

    private String formatHistoryLine(Message message) {
        return "HISTORY_ITEM|" + message.getSender().getUsername()
                + "|" + message.getRecipient().getUsername()
                + "|" + TS.format(message.getTimestamp())
                + "|" + Protocol.encode(message.getContent());
    }

    private boolean isAuthenticated(PrintWriter out) {
        if (loggedUsername == null) {
            out.println("ERR|UNAUTHORIZED|Authentification requise");
            return false;
        }
        return true;
    }

    private void cleanupSession() {
        // RG4/RG10: au départ (ou perte réseau), statut OFFLINE + notification globale.
        if (loggedUsername == null) {
            return;
        }
        String username = loggedUsername;
        loggedUsername = null;
        services.sessionRegistry().unregister(username);
        services.authService().markOffline(username);
        broadcastStatus(username, "OFFLINE");
        LOGGER.info("Deconnexion: {}", username);
    }

    private void broadcastStatus(String username, String status) {
        for (ClientSession session : services.sessionRegistry().allSessions()) {
            session.sendLine("USER_STATUS|" + username + "|" + status);
        }
    }

    private String sanitize(String text) {
        return text.replace("\n", " ").replace("\r", " ");
    }

    private String safeDecode(String value, String label) {
        try {
            return Protocol.decode(value);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Decode Base64 invalide pour {}: {}", label, e.getMessage());
            return null;
        }
    }
}

