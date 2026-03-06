package client.network;

import javafx.application.Platform;
import client.model.ChatMessageView;
import client.model.UserView;
import util.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ServerConnection - composant principal de l'application.
 */
public class ServerConnection {
    // Adresse du serveur cible.
    private final String host;
    // Port TCP du serveur.
    private final int port;
    // Listener courant qui recoit les evenements reseau.
    private volatile ServerEventListener listener;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread listenerThread;
    private volatile boolean running;
    // Quand true, les MESSAGE entrants sont mis en tampon.
    private volatile boolean bufferIncomingMessages = true;
    // Tampon temporaire pour ne pas perdre de messages pendant les transitions d'ecran.
    private final List<ChatMessageView> bufferedMessages = Collections.synchronizedList(new ArrayList<>());

    public ServerConnection(String host, int port, ServerEventListener listener) {
        // Injection des parametres de connexion et du listener initial.
        this.host = host;
        this.port = port;
        this.listener = listener;
    }

    public void setListener(ServerEventListener listener, boolean shouldFlush) {
        // Permet de changer dynamiquement le receveur des callbacks (LoginController/MainController).
        this.listener = listener;
        if (shouldFlush) {
            // Optionnel: vide le tampon des messages accumules.
            flushBufferedMessages();
        }
    }

    public void setBufferIncomingMessages(boolean enabled) {
        // Active/desactive la mise en tampon des messages entrants.
        this.bufferIncomingMessages = enabled;
    }

    public void connect() throws IOException {
        // Ouverture de la socket client vers le serveur.
        socket = new Socket(host, port);
        // Flux texte entrant.
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        // Flux texte sortant (autoFlush=true).
        out = new PrintWriter(socket.getOutputStream(), true);
        running = true;
        // Thread dedie a la lecture continue des lignes serveur.
        listenerThread = new Thread(this::readLoop, "server-listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public boolean isConnected() {
        // Etat socket: creee, connectee et non fermee.
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void login(String username, String password) {
        // Envoi de la commande LOGIN avec mot de passe encode.
        send(Protocol.join("LOGIN", username, Protocol.encode(password)));
    }

    public void register(String username, String password, String role) {
        // Envoi de la commande REGISTER.
        send(Protocol.join("REGISTER", username, Protocol.encode(password), role));
    }

    public void requestUsers() {
        // Demande la liste des utilisateurs visibles.
        send("LIST_USERS");
    }

    public void requestHistory(String otherUser) {
        // Demande l'historique avec un interlocuteur.
        send(Protocol.join("HISTORY", otherUser));
    }

    public void sendMessage(String recipient, String content) {
        // Envoi d'un message texte encode.
        send(Protocol.join("MSG", recipient, Protocol.encode(content)));
    }

    public void logout() {
        // Demande fermeture de session puis fermeture locale.
        send("LOGOUT");
        close();
    }

    public void close() {
        // Arrete la boucle de lecture et ferme la socket.
        running = false;
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }

    private synchronized void send(String line) {
        // Synchronise pour eviter l'entrelacement de lignes depuis plusieurs appels.
        if (out != null) {
            out.println(line);
        }
    }

    private void readLoop() {
        // Tampons temporaires pour les reponses multi-lignes (USERS / HISTORY).
        List<UserView> usersBuffer = null;
        String pendingHistoryPartner = null;
        List<ChatMessageView> historyBuffer = null;

        try {
            String line;
            while (running && (line = in.readLine()) != null) {
                // Parse brut de la ligne protocole.
                String[] tokens = line.split("\\|");
                if (tokens.length == 0) {
                    continue;
                }
                String command = tokens[0];

                switch (command) {
                    // INFO: message informatif simple.
                    case "INFO" -> runFx(() -> listener.onInfo(tokens.length > 1 ? tokens[1] : ""));
                    case "OK" -> {
                        // LOGIN valide: retourne username + role.
                        if (tokens.length > 1 && "LOGIN".equals(tokens[1]) && tokens.length >= 4) {
                            runFx(() -> listener.onLoginSuccess(tokens[2], tokens[3]));
                        // REGISTER valide.
                        } else if (tokens.length > 1 && "REGISTER".equals(tokens[1]) && tokens.length >= 3) {
                            runFx(() -> listener.onRegisterSuccess(tokens[2]));
                        // Debut de flux LIST_USERS.
                        } else if (tokens.length > 1 && "LIST_USERS".equals(tokens[1])) {
                            usersBuffer = new ArrayList<>();
                        // Debut de flux HISTORY.
                        } else if (tokens.length > 1 && "HISTORY".equals(tokens[1]) && tokens.length >= 3) {
                            pendingHistoryPartner = tokens[2];
                            historyBuffer = new ArrayList<>();
                        }
                    }
                    case "USERS_ITEM" -> {
                        // Element de liste utilisateur.
                        if (usersBuffer != null && tokens.length >= 4) {
                            usersBuffer.add(new UserView(tokens[1], tokens[2], tokens[3]));
                        }
                    }
                    case "END" -> {
                        // Fin de flux utilisateurs.
                        if (tokens.length > 1 && "USERS".equals(tokens[1]) && usersBuffer != null) {
                            List<UserView> users = usersBuffer;
                            usersBuffer = null;
                            runFx(() -> listener.onUsersReceived(users));
                        }
                        // Fin de flux historique.
                        if (tokens.length > 1 && "HISTORY".equals(tokens[1]) && historyBuffer != null) {
                            String partner = pendingHistoryPartner;
                            List<ChatMessageView> history = historyBuffer;
                            pendingHistoryPartner = null;
                            historyBuffer = null;
                            runFx(() -> listener.onHistoryReceived(partner, history));
                        }
                    }
                    case "HISTORY_ITEM" -> {
                        // Element d'historique.
                        if (historyBuffer != null && tokens.length >= 5) {
                            historyBuffer.add(parseMessage(tokens));
                        }
                    }
                    case "MESSAGE" -> {
                        // Message temps reel.
                        if (tokens.length >= 5) {
                            ChatMessageView message = parseMessage(tokens);
                            if (bufferIncomingMessages) {
                                // Stocke temporairement si buffering actif.
                                bufferedMessages.add(message);
                            } else {
                                // Notifie directement l'UI.
                                runFx(() -> listener.onMessageReceived(message));
                            }
                        }
                    }
                    case "USER_STATUS" -> {
                        // Changement online/offline.
                        if (tokens.length >= 3) {
                            runFx(() -> listener.onUserStatus(tokens[1], tokens[2]));
                        }
                    }
                    case "ERR" -> {
                        // Erreur generique serveur.
                        String code = tokens.length > 1 ? tokens[1] : "UNKNOWN";
                        String msg = tokens.length > 2 ? tokens[2] : "Erreur inconnue";
                        runFx(() -> listener.onError(code, msg));
                    }
                    default -> {
                    }
                }
            }
            // Sortie naturelle de boucle: serveur ferme la connexion.
            runFx(() -> listener.onDisconnected("Connexion fermee par le serveur"));
        } catch (Exception e) {
            // Erreur reseau inattendue (timeout, reset, etc.).
            runFx(() -> listener.onDisconnected("Connexion perdue: " + e.getMessage()));
        } finally {
            // Marque l'arret de la connexion.
            running = false;
        }
    }

    private ChatMessageView parseMessage(String[] tokens) {
        // Conversion tokens protocole -> record UI.
        return new ChatMessageView(
                tokens[1],
                tokens[2],
                LocalDateTime.parse(tokens[3]),
                Protocol.decode(tokens[4])
        );
    }

    private void runFx(Runnable runnable) {
        // Toute maj UI JavaFX doit passer par le thread FX.
        if (listener != null) {
            Platform.runLater(runnable);
        }
    }

    public void flushBufferedMessages() {
        // Si pas de listener, impossible de livrer.
        if (listener == null) {
            return;
        }
        List<ChatMessageView> snapshot;
        synchronized (bufferedMessages) {
            // Rien a flusher.
            if (bufferedMessages.isEmpty()) {
                return;
            }
            // Snapshot pour liberer rapidement le verrou.
            snapshot = new ArrayList<>(bufferedMessages);
            bufferedMessages.clear();
        }
        for (ChatMessageView message : snapshot) {
            // Livraison sequentielle des messages tamponnes.
            runFx(() -> listener.onMessageReceived(message));
        }
    }
}

