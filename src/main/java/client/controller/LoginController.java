package client.controller;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import client.AppContext;
import client.network.ServerConnection;
import client.network.ServerEventListener;
import client.model.ChatMessageView;
import client.model.UserView;

import java.io.IOException;
import java.util.List;

/**
 * ControleurConnexion - Gère l'écran de connexion de l'application.
 */
public class LoginController implements ServerEventListener {
    // Champs de l'interface graphique liés au fichier FXML
    @FXML
    private TextField champNomUtilisateur;
    @FXML
    private PasswordField champMotDePasse;
    @FXML
    private Label labelRetour;

    /**
     * INITIALISATION DE L'ÉCRAN DE CONNEXION
     * 
     * Cette méthode est appelée automatiquement par JavaFX après le chargement du fichier FXML.
     * Elle initialise l'interface en affichant soit un message flash (après inscription),
     * soit l'adresse du serveur par défaut.
     * 
     * Fonctionnement :
     * 1. Récupère un éventuel message flash depuis AppContext
     * 2. Affiche ce message ou "Serveur: localhost:8088" par défaut
     */
    @FXML
    public void initialize() {
        // Affiche le message de retour ou l'état par défaut
        String messageFlash = AppContext.consumeFlashMessage();
        labelRetour.setText(messageFlash != null ? messageFlash : "Serveur: localhost:8088");
    }

    /**
     * GESTION DU BOUTON "SE CONNECTER"
     * 
     * Cette méthode est déclenchée quand l'utilisateur clique sur le bouton "SE CONNECTER".
     * Elle valide les champs du formulaire puis tente de se connecter au serveur.
     * 
     * Étapes :
     * 1. Récupère le nom d'utilisateur et le mot de passe saisis
     * 2. Vérifie que les champs ne sont pas vides
     * 3. Si vides : affiche un message d'erreur et secoue les champs
     * 4. Si valides : établit une connexion au serveur
     * 5. Envoie les identifiants au serveur pour authentification
     * 6. En cas d'erreur réseau : affiche un message d'erreur
     */
    @FXML
    private void seConnecterAuServeur() {
        // Validation du formulaire
        String nomUtilisateur = champNomUtilisateur.getText();
        String motDePasse = champMotDePasse.getText();
        if (nomUtilisateur == null || nomUtilisateur.isBlank() || motDePasse == null || motDePasse.isBlank()) {
            labelRetour.setText("Nom d'utilisateur et mot de passe requis.");
            animerSecousseErreur(champNomUtilisateur, champMotDePasse);
            return;
        }
        try {
            // Connexion au serveur
            ServerConnection connexion = assurerConnexion();
            AppContext.setCredentials(nomUtilisateur.trim(), motDePasse);
            connexion.login(nomUtilisateur.trim(), motDePasse);
        } catch (IOException e) {
            labelRetour.setText("ERREUR: Serveur non disponible. Démarrez le serveur d'abord!");
            animerSecousseErreur(champNomUtilisateur, champMotDePasse);
        }
    }

    /**
     * NAVIGATION VERS L'ÉCRAN D'INSCRIPTION
     * 
     * Cette méthode est déclenchée quand l'utilisateur clique sur "S'INSCRIRE".
     * Elle charge et affiche l'écran d'inscription (register.fxml).
     * 
     * Fonctionnement :
     * 1. Demande à AppContext de charger l'écran d'inscription
     * 2. Si erreur de chargement : affiche un message d'erreur
     */
    @FXML
    private void allerVersEcranInscription() {
        // Navigation vers l'écran d'inscription
        try {
            AppContext.showRegister();
        } catch (IOException e) {
            labelRetour.setText("Erreur UI: " + e.getMessage());
        }
    }

    /**
     * RETOUR À L'ÉCRAN D'ACCUEIL
     * 
     * Cette méthode est déclenchée quand l'utilisateur clique sur "RETOUR".
     * Elle charge et affiche l'écran d'accueil (welcome.fxml).
     * 
     * Fonctionnement :
     * 1. Demande à AppContext de charger l'écran d'accueil
     * 2. Si erreur de chargement : affiche un message d'erreur
     */
    @FXML
    private void retourVersEcranAccueil() {
        // Retour vers l'écran d'accueil
        try {
            AppContext.showWelcome();
        } catch (IOException e) {
            labelRetour.setText("Erreur UI: " + e.getMessage());
        }
    }

    /**
     * BASCULER ENTRE THÈME CLAIR ET SOMBRE
     * 
     * Cette méthode est déclenchée quand l'utilisateur clique sur "Mode sombre".
     * Elle bascule l'apparence de l'application entre le thème clair et le thème sombre.
     * 
     * Fonctionnement :
     * 1. Demande à AppContext de basculer le thème
     * 2. Le changement est appliqué immédiatement à toute l'interface
     */
    @FXML
    private void basculerEntreThemeClairEtSombre() {
        // Bascule entre thème clair et sombre
        AppContext.toggleTheme();
    }

    /**
     * ÉTABLIR OU RÉUTILISER UNE CONNEXION AU SERVEUR
     * 
     * Cette méthode s'assure qu'une connexion active au serveur existe.
     * Si aucune connexion n'existe ou si elle est fermée, elle en crée une nouvelle.
     * Sinon, elle réutilise la connexion existante.
     * 
     * Fonctionnement :
     * 1. Vérifie si une connexion existe déjà dans AppContext
     * 2. Si non ou si déconnectée :
     *    - Crée une nouvelle connexion socket vers localhost:8088
     *    - Établit la connexion
     *    - Stocke la connexion dans AppContext
     * 3. Si oui :
     *    - Réutilise la connexion existante
     *    - Met à jour le listener pour recevoir les événements
     * 
     * @return La connexion au serveur (nouvelle ou existante)
     * @throws IOException Si la connexion au serveur échoue
     */
    private ServerConnection assurerConnexion() throws IOException {
        ServerConnection connexion = AppContext.connection();
        if (connexion == null || !connexion.isConnected()) {
            // Première connexion au serveur
            connexion = new ServerConnection("localhost", 8088, this);
            connexion.connect();
            AppContext.setConnection(connexion);
        } else {
            // Connexion déjà ouverte
            connexion.setListener(this, false);
        }
        return connexion;
    }

    /**
     * CALLBACK : CONNEXION RÉUSSIE
     * 
     * Cette méthode est appelée automatiquement par le serveur quand l'authentification réussit.
     * Elle stocke l'identité de l'utilisateur et ouvre l'écran principal de messagerie.
     * 
     * Fonctionnement :
     * 1. Stocke le nom d'utilisateur et le rôle dans AppContext
     * 2. Affiche un message de succès
     * 3. Charge l'écran principal (main.fxml)
     * 4. Configure le contrôleur principal pour recevoir les événements réseau
     * 5. Désactive le buffer de messages entrants
     * 6. Demande la liste des utilisateurs au serveur
     * 
     * @param nomUtilisateur Le nom de l'utilisateur connecté
     * @param role Le rôle de l'utilisateur (ORGANISATEUR, MEMBRE, BENEVOLE)
     */
    @Override
    public void onLoginSuccess(String nomUtilisateur, String role) {
        // Connexion réussie - ouverture de l'écran principal
        AppContext.setIdentity(nomUtilisateur, role);
        labelRetour.setText("Connexion réussie.");
        try {
            MainController controleurPrincipal = AppContext.showMain();
            AppContext.connection().setListener(controleurPrincipal, true);
            AppContext.connection().setBufferIncomingMessages(false);
            AppContext.connection().requestUsers();
        } catch (IOException e) {
            labelRetour.setText("Erreur UI: " + e.getMessage());
        }
    }

    /**
     * CALLBACK : INSCRIPTION RÉUSSIE
     * 
     * Cette méthode est appelée automatiquement par le serveur quand l'inscription réussit.
     * Elle prépare un message de succès et redirige vers l'écran de connexion.
     * 
     * Fonctionnement :
     * 1. Crée un message flash de succès
     * 2. Stocke ce message dans AppContext
     * 3. Redirige vers l'écran de connexion
     * 4. Le message sera affiché sur l'écran de connexion
     * 
     * @param nomUtilisateur Le nom de l'utilisateur qui vient de s'inscrire
     */
    @Override
    public void onRegisterSuccess(String nomUtilisateur) {
        // Inscription réussie - redirection vers connexion
        AppContext.setFlashMessage("Inscription réussie pour " + nomUtilisateur + ". Connectez-vous.");
        try {
            AppContext.showLogin();
        } catch (IOException ignored) {
            // Erreur non critique
        }
    }

    /**
     * CALLBACK : ERREUR DU SERVEUR
     * 
     * Cette méthode est appelée automatiquement quand le serveur renvoie une erreur.
     * Elle affiche le message d'erreur et secoue les champs pour attirer l'attention.
     * 
     * Exemples d'erreurs :
     * - "AUTH_FAILED: Identifiants incorrects"
     * - "USER_EXISTS: Ce nom d'utilisateur existe déjà"
     * - "INVALID_REQUEST: Requête mal formée"
     * 
     * Fonctionnement :
     * 1. Affiche le code d'erreur et le message dans le label
     * 2. Secoue les champs de saisie pour signaler l'erreur visuellement
     * 
     * @param code Le code d'erreur (ex: AUTH_FAILED, USER_EXISTS)
     * @param message Le message d'erreur détaillé
     */
    @Override
    public void onError(String code, String message) {
        // Gestion des erreurs du serveur
        labelRetour.setText(code + ": " + message);
        animerSecousseErreur(champNomUtilisateur, champMotDePasse);
    }

    /**
     * CALLBACK : MESSAGE INFORMATIF DU SERVEUR
     * 
     * Cette méthode est appelée automatiquement quand le serveur envoie un message informatif.
     * Elle affiche simplement le message dans le label de retour.
     * 
     * Exemples de messages :
     * - "Connexion établie"
     * - "Serveur prêt"
     * - "Bienvenue sur le serveur"
     * 
     * @param texte Le message informatif à afficher
     */
    @Override
    public void onInfo(String texte) {
        // Message informatif du serveur
        labelRetour.setText(texte);
    }

    /**
     * CALLBACK : DÉCONNEXION DU SERVEUR
     * 
     * Cette méthode est appelée automatiquement quand la connexion au serveur est perdue.
     * Elle affiche la raison de la déconnexion.
     * 
     * Exemples de raisons :
     * - "Serveur arrêté"
     * - "Connexion perdue"
     * - "Timeout"
     * 
     * @param raison La raison de la déconnexion
     */
    @Override
    public void onDisconnected(String raison) {
        // Déconnexion du serveur
        labelRetour.setText(raison);
    }

    /**
     * CALLBACK : LISTE DES UTILISATEURS REÇUE
     * 
     * Cette méthode est appelée quand le serveur envoie la liste des utilisateurs.
     * Elle n'est pas utilisée dans l'écran de connexion (uniquement dans l'écran principal).
     * 
     * @param utilisateurs La liste des utilisateurs connectés
     */
    @Override
    public void onUsersReceived(List<UserView> utilisateurs) {
        // Non utilisé dans l'écran de connexion
    }

    /**
     * CALLBACK : MESSAGE REÇU
     * 
     * Cette méthode est appelée quand un message de chat est reçu.
     * Elle n'est pas utilisée dans l'écran de connexion (uniquement dans l'écran principal).
     * 
     * @param message Le message de chat reçu
     */
    @Override
    public void onMessageReceived(ChatMessageView message) {
        // Non utilisé dans l'écran de connexion
    }

    /**
     * CALLBACK : HISTORIQUE DE CONVERSATION REÇU
     * 
     * Cette méthode est appelée quand le serveur envoie l'historique d'une conversation.
     * Elle n'est pas utilisée dans l'écran de connexion (uniquement dans l'écran principal).
     * 
     * @param partenaire Le nom de l'utilisateur avec qui on a conversé
     * @param historique La liste des messages de la conversation
     */
    @Override
    public void onHistoryReceived(String partenaire, List<ChatMessageView> historique) {
        // Non utilisé dans l'écran de connexion
    }

    /**
     * CALLBACK : CHANGEMENT DE STATUT D'UN UTILISATEUR
     * 
     * Cette méthode est appelée quand un utilisateur change de statut (ONLINE/OFFLINE).
     * Elle n'est pas utilisée dans l'écran de connexion (uniquement dans l'écran principal).
     * 
     * @param nomUtilisateur Le nom de l'utilisateur dont le statut a changé
     * @param statut Le nouveau statut (ONLINE ou OFFLINE)
     */
    @Override
    public void onUserStatus(String nomUtilisateur, String statut) {
        // Non utilisé dans l'écran de connexion
    }

    /**
     * ANIMATION DE SECOUSSE POUR SIGNALER UNE ERREUR
     * 
     * Cette méthode crée une animation de secousse sur les éléments d'interface passés en paramètre.
     * Elle est utilisée pour attirer visuellement l'attention de l'utilisateur sur une erreur.
     * 
     * Fonctionnement de l'animation :
     * 1. Déplace l'élément de -5 pixels vers la gauche
     * 2. Puis de +5 pixels vers la droite
     * 3. Répète ce mouvement 4 fois (8 mouvements au total)
     * 4. Chaque mouvement dure 50 millisecondes
     * 5. L'animation totale dure donc 400ms (0.4 seconde)
     * 
     * Utilisation typique :
     * - Champs vides lors de la connexion
     * - Identifiants incorrects
     * - Erreur de validation
     * 
     * @param noeuds Les éléments d'interface à secouer (champs de texte, labels, etc.)
     */
    private void animerSecousseErreur(Node... noeuds) {
        // Animation de secousse pour signaler une erreur
        for (Node noeud : noeuds) {
            TranslateTransition transition = new TranslateTransition(Duration.millis(50), noeud);
            transition.setFromX(-5);
            transition.setToX(5);
            transition.setCycleCount(4);
            transition.setAutoReverse(true);
            transition.play();
        }
    }
}

