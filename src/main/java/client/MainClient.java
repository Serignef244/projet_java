package client;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * MainClient - composant principal de l'application.
 */
public class MainClient extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Initialise le contexte global JavaFX avec la fenetre principale.
        AppContext.init(stage);
        // Affiche l'ecran d'accueil au lancement.
        AppContext.showWelcome();
    }

    @Override
    public void stop() {
        // Appel automatique a la fermeture de l'application.
        if (AppContext.connection() != null) {
            try {
                // Ferme proprement la session cote serveur.
                AppContext.connection().logout();
            } catch (Exception ignored) {
                // Fallback: fermeture locale de la socket si logout echoue.
                AppContext.connection().close();
            }
        }
    }

    public static void main(String[] args) {
        // Point d'entree JavaFX.
        launch(args);
    }
}

