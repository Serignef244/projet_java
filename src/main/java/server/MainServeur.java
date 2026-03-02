package server;

import config.JpaUtil;
import dao.MessageRepository;
import dao.UserRepository;
import server.handler.ClientHandler;
import service.ServerServices;
import service.SessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MainServeur - composant principal de l'application.
 */
public class MainServeur {
    // Logger applicatif serveur.
    private static final Logger LOGGER = LoggerFactory.getLogger(MainServeur.class);
    // Port par defaut si aucun argument n'est fourni.
    private static final int DEFAULT_PORT = 8088;

    public static void main(String[] args) {
        // Resolution du port de lancement.
        int port = resolvePort(args);
        // Registre des sessions actives (utilisateurs connectes).
        SessionRegistry sessionRegistry = new SessionRegistry();
        // Repositories relies a l'EntityManagerFactory JPA.
        UserRepository userRepository = new UserRepository(JpaUtil.emf());
        MessageRepository messageRepository = new MessageRepository(JpaUtil.emf());
        // Au demarrage, tous les utilisateurs sont forces OFFLINE.
        userRepository.markAllOffline();
        // Assembleur des services metier utilises par ClientHandler.
        ServerServices services = new ServerServices(sessionRegistry, userRepository, messageRepository);

        // Un thread dedie par client via pool dynamique.
        ExecutorService pool = Executors.newCachedThreadPool();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Fermeture propre en cas d'arret JVM (Ctrl+C, kill, etc.).
            pool.shutdownNow();
            JpaUtil.shutdown();
            LOGGER.info("Serveur arrete.");
        }));

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // Socket d'ecoute principal.
            LOGGER.info("Serveur de messagerie demarre sur le port {}", port);
            while (true) {
                // Accepte chaque connexion entrante.
                Socket socket = serverSocket.accept();
                // Cree un handler independant pour ce client.
                pool.execute(new ClientHandler(socket, services));
            }
        } catch (IOException e) {
            // Erreur bloquante du serveur principal.
            LOGGER.error("Erreur serveur: {}", e.getMessage(), e);
        } finally {
            // Liberation des ressources si sortie de la boucle.
            pool.shutdownNow();
            JpaUtil.shutdown();
        }
    }

    private static int resolvePort(String[] args) {
        // Autorise un port en argument CLI.
        if (args.length > 0) {
            try {
                return Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                // Fallback automatique sur le port par defaut.
                LOGGER.warn("Port invalide '{}', fallback {}", args[0], DEFAULT_PORT);
            }
        }
        return DEFAULT_PORT;
    }
}

