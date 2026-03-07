package client;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.util.Duration;
import client.controller.LoginController;
import client.controller.RegisterController;
import client.controller.WelcomeController;
import client.controller.MainController;
import client.network.ServerConnection;

import java.io.IOException;

/**
 * AppContext - composant principal de l'application.
 */
public final class AppContext {
    // Etat global client partage entre les ecrans JavaFX.
    private static Stage stage;
    private static ServerConnection connection;
    private static String loggedUser;
    private static String loggedRole;
    private static String authUsername;
    private static String authPassword;
    private static String flashMessage;
    private static boolean darkMode;

    private AppContext() {
    }

    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    public static Stage stage() {
        return stage;
    }

    public static ServerConnection connection() {
        return connection;
    }

    public static void setConnection(ServerConnection conn) {
        connection = conn;
    }

    public static String loggedUser() {
        return loggedUser;
    }

    public static String loggedRole() {
        return loggedRole;
    }

    public static void setIdentity(String user, String role) {
        loggedUser = user;
        loggedRole = role;
    }

    public static void setCredentials(String username, String password) {
        authUsername = username;
        authPassword = password;
    }

    public static String authUsername() {
        return authUsername;
    }

    public static String authPassword() {
        return authPassword;
    }

    public static void clearIdentity() {
        loggedUser = null;
        loggedRole = null;
        authUsername = null;
        authPassword = null;
    }

    public static void setFlashMessage(String message) {
        flashMessage = message;
    }

    public static String consumeFlashMessage() {
        String msg = flashMessage;
        flashMessage = null;
        return msg;
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void toggleTheme() {
        // Change le mode visuel puis reapplique le CSS sur la scene active.
        darkMode = !darkMode;
        if (stage != null && stage.getScene() != null) {
            applyStyle(stage.getScene());
        }
    }

    public static WelcomeController showWelcome() throws IOException {
        // Charge la vue accueil puis configure la fenetre.
        FXMLLoader loader = new FXMLLoader(AppContext.class.getResource("/fxml/welcome.fxml"));
        Scene scene = new Scene(loader.load(), 460, 320);
        applyStyle(scene);
        stage.setTitle("Messagerie Association - Accueil");
        stage.setResizable(true);
        stage.setMinWidth(340);
        stage.setMinHeight(300);
        stage.setScene(scene);
        animateScene(scene.getRoot());
        stage.show();
        return loader.getController();
    }

    public static LoginController showLogin() throws IOException {
        // Charge la vue connexion puis configure la fenetre.
        FXMLLoader loader = new FXMLLoader(AppContext.class.getResource("/fxml/login-view.fxml"));
        Scene scene = new Scene(loader.load(), 520, 380);
        applyStyle(scene);
        stage.setTitle("Messagerie Association - Connexion");
        stage.setResizable(true);
        stage.setMinWidth(360);
        stage.setMinHeight(340);
        stage.setScene(scene);
        animateScene(scene.getRoot());
        stage.show();
        return loader.getController();
    }

    public static RegisterController showRegister() throws IOException {
        // Charge la vue inscription puis configure la fenetre.
        FXMLLoader loader = new FXMLLoader(AppContext.class.getResource("/fxml/register-view.fxml"));
        Scene scene = new Scene(loader.load(), 620, 500);
        applyStyle(scene);
        stage.setTitle("Messagerie Association - Inscription");
        stage.setResizable(true);
        stage.setMinWidth(380);
        stage.setMinHeight(420);
        stage.setScene(scene);
        animateScene(scene.getRoot());
        stage.show();
        return loader.getController();
    }

    public static MainController showMain() throws IOException {
        // Charge la vue principale (messagerie) puis configure la fenetre.
        FXMLLoader loader = new FXMLLoader(AppContext.class.getResource("/fxml/chat-view.fxml"));
        Scene scene = new Scene(loader.load(), 1280, 760);
        applyStyle(scene);
        stage.setTitle("Messagerie Association - Principal");
        stage.setResizable(true);
        stage.setMinWidth(360);
        stage.setMinHeight(600);
        stage.setScene(scene);
        animateScene(scene.getRoot());
        stage.show();
        return loader.getController();
    }

    private static void applyStyle(Scene scene) {
        // Ajoute la feuille de style et applique la classe dark-mode si necessaire.
        String css = AppContext.class.getResource("/styles/styles.css").toExternalForm();
        if (!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }
        scene.getRoot().getStyleClass().remove("dark-mode");
        if (darkMode) {
            scene.getRoot().getStyleClass().add("dark-mode");
        }
    }

    private static void animateScene(Parent root) {
        // Transition d'apparition pour fluidifier les changements d'ecran.
        root.setOpacity(0);
        FadeTransition transition = new FadeTransition(Duration.millis(260), root);
        transition.setFromValue(0);
        transition.setToValue(1);
        transition.play();
    }
}

