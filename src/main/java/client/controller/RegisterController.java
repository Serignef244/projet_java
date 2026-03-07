package client.controller;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import client.AppContext;
import client.model.ChatMessageView;
import client.model.UserView;
import client.network.ServerConnection;
import client.network.ServerEventListener;

import java.io.IOException;
import java.util.List;

/**
 * ControleurInscription - Gère l'écran d'inscription de l'application.
 * 
 * CE CONTRÔLEUR PERMET :
 * - Créer un nouveau compte utilisateur
 * - Choisir un rôle (ORGANISATEUR, MEMBRE, BENEVOLE)
 * - Valider les informations saisies
 * - Communiquer avec le serveur pour l'inscription
 * - Rediriger vers l'écran de connexion après succès
 */
public class RegisterController implements ServerEventListener {
    @FXML
    private TextField champNomUtilisateur;
    @FXML
    private PasswordField champMotDePasse;
    @FXML
    private PasswordField champConfirmationMotDePasse;
    @FXML
    private ChoiceBox<String> choixRole;
    @FXML
    private Label labelRetour;

    /**
     * INITIALISATION DE L'ÉCRAN D'INSCRIPTION
     * 
     * Cette méthode est appelée automatiquement par JavaFX après le chargement du fichier FXML.
     * Elle initialise la liste des rôles disponibles et sélectionne "MEMBRE" par défaut.
     * 
     * Fonctionnement :
     * 1. Ajoute les 3 rôles disponibles dans la liste déroulante :
     *    - ORGANISATEUR : Accès complet + gestion des membres
     *    - MEMBRE : Accès standard à la messagerie
     *    - BENEVOLE : Accès standard à la messagerie
     * 2. Sélectionne "MEMBRE" comme rôle par défaut
     * 3. Affiche un message d'instruction
     */
    @FXML
    public void initialize() {
        // Chargement des rôles disponibles
        choixRole.getItems().addAll("ORGANISATEUR", "MEMBRE", "BENEVOLE");
        choixRole.getSelectionModel().select("MEMBRE");
        labelRetour.setText("Complétez le formulaire d'inscription.");
    }

    /**
     * GESTION DU BOUTON "VALIDER L'INSCRIPTION"
     * 
     * Cette méthode est déclenchée quand l'utilisateur clique sur "VALIDER L'INSCRIPTION".
     * Elle effectue plusieurs validations avant d'envoyer la demande au serveur.
     * 
     * VALIDATIONS EFFECTUÉES :
     * 1. Vérifie que le nom d'utilisateur n'est pas vide
     * 2. Vérifie que le mot de passe n'est pas vide
     * 3. Vérifie que les deux mots de passe correspondent
     * 
     * SI VALIDATIONS OK :
     * 1. Établit une connexion au serveur
     * 2. Envoie la demande d'inscription avec :
     *    - Nom d'utilisateur
     *    - Mot de passe
     *    - Rôle choisi
     * 
     * SI ERREUR :
     * - Affiche un message d'erreur explicite
     * - Secoue les champs concernés pour attirer l'attention
     */
    @FXML
    private void validerFormulaireInscription() {
        // Validation du formulaire
        String nomUtilisateur = champNomUtilisateur.getText();
        String motDePasse = champMotDePasse.getText();
        String confirmation = champConfirmationMotDePasse.getText();

        if (nomUtilisateur == null || nomUtilisateur.isBlank() || motDePasse == null || motDePasse.isBlank()) {
            labelRetour.setText("Nom d'utilisateur et mot de passe requis.");
            animerSecousseErreur(champNomUtilisateur, champMotDePasse, champConfirmationMotDePasse);
            return;
        }
        if (!motDePasse.equals(confirmation)) {
            labelRetour.setText("La confirmation du mot de passe ne correspond pas.");
            animerSecousseErreur(champMotDePasse, champConfirmationMotDePasse);
            return;
        }
        try {
            // Envoi de la demande d'inscription au serveur
            ServerConnection connexion = assurerConnexion();
            connexion.register(nomUtilisateur.trim(), motDePasse, choixRole.getValue());
        } catch (IOException e) {
            labelRetour.setText("ERREUR: Serveur non disponible. Démarrez le serveur d'abord!");
            animerSecousseErreur(champNomUtilisateur, champMotDePasse, champConfirmationMotDePasse);
        }
    }

    /**
     * RETOUR À L'ÉCRAN DE CONNEXION
     * 
     * Cette méthode est déclenchée quand l'utilisateur clique sur "RETOUR CONNEXION".
     * Elle charge et affiche l'écran de connexion (login.fxml).
     * 
     * Utilisation typique :
     * - L'utilisateur a déjà un compte et veut se connecter
     * - L'utilisateur veut annuler l'inscription
     * 
     * @throws IOException Si le fichier login.fxml ne peut pas être chargé
     */
    @FXML
    private void retourVersEcranConnexion() {
        try {
            AppContext.showLogin();
        } catch (IOException e) {
            labelRetour.setText("Erreur UI: " + e.getMessage());
        }
    }

    /**
     * BASCULER ENTRE THÈME CLAIR ET SOMBRE
     * 
     * Cette méthode est déclenchée quand l'utilisateur clique sur "Mode sombre".
     * Elle bascule l'apparence de l'application entre le thème clair et le thème sombre.
     * Le changement est appliqué immédiatement à toute l'interface.
     */
    @FXML
    private void basculerEntreThemeClairEtSombre() {
        AppContext.toggleTheme();
    }

    /**
     * ÉTABLIR OU RÉUTILISER UNE CONNEXION AU SERVEUR
     * 
     * Cette méthode s'assure qu'une connexion active au serveur existe.
     * Elle fonctionne de la même manière que dans LoginController.
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
            connexion = new ServerConnection("localhost", 8088, this);
            connexion.connect();
            AppContext.setConnection(connexion);
        } else {
            connexion.setListener(this, false);
        }
        return connexion;
    }

    /**
     * CALLBACK : INSCRIPTION RÉUSSIE
     * 
     * Cette méthode est appelée automatiquement par le serveur quand l'inscription réussit.
     * Elle prépare un message de succès et redirige vers l'écran de connexion.
     * 
     * Fonctionnement :
     * 1. Crée un message flash : "Inscription réussie pour [nom]. Connectez-vous."
     * 2. Stocke ce message dans AppContext
     * 3. Redirige vers l'écran de connexion (login.fxml)
     * 4. Le message sera affiché automatiquement sur l'écran de connexion
     * 
     * @param nomUtilisateur Le nom de l'utilisateur qui vient de s'inscrire
     */
    @Override
    public void onRegisterSuccess(String nomUtilisateur) {
        // Redirection vers connexion avec message de succès
        AppContext.setFlashMessage("Inscription réussie pour " + nomUtilisateur + ". Connectez-vous.");
        try {
            AppContext.showLogin();
        } catch (IOException ignored) {
        }
    }

    /**
     * CALLBACK : ERREUR DU SERVEUR
     * 
     * Cette méthode est appelée automatiquement quand le serveur renvoie une erreur.
     * 
     * ERREURS POSSIBLES LORS DE L'INSCRIPTION :
     * - "USER_EXISTS: Ce nom d'utilisateur existe déjà"
     * - "INVALID_USERNAME: Nom d'utilisateur invalide"
     * - "WEAK_PASSWORD: Mot de passe trop faible"
     * - "INVALID_ROLE: Rôle non reconnu"
     * 
     * Fonctionnement :
     * 1. Affiche le code d'erreur et le message dans le label
     * 2. Secoue tous les champs de saisie pour signaler l'erreur visuellement
     * 
     * @param code Le code d'erreur (ex: USER_EXISTS, INVALID_USERNAME)
     * @param message Le message d'erreur détaillé
     */
    @Override
    public void onError(String code, String message) {
        labelRetour.setText(code + ": " + message);
        animerSecousseErreur(champNomUtilisateur, champMotDePasse, champConfirmationMotDePasse);
    }

    /**
     * CALLBACK : MESSAGE INFORMATIF DU SERVEUR
     * 
     * Cette méthode est appelée automatiquement quand le serveur envoie un message informatif.
     * Elle affiche simplement le message dans le label de retour.
     * 
     * @param texte Le message informatif à afficher
     */
    @Override
    public void onInfo(String texte) {
        labelRetour.setText(texte);
    }

    /**
     * CALLBACK : DÉCONNEXION DU SERVEUR
     * 
     * Cette méthode est appelée automatiquement quand la connexion au serveur est perdue.
     * Elle affiche la raison de la déconnexion.
     * 
     * @param raison La raison de la déconnexion
     */
    @Override
    public void onDisconnected(String raison) {
        labelRetour.setText(raison);
    }

    /**
     * CALLBACK : CONNEXION RÉUSSIE
     * Non utilisé dans l'écran d'inscription.
     */
    @Override
    public void onLoginSuccess(String nomUtilisateur, String role) {
    }

    /**
     * CALLBACK : LISTE DES UTILISATEURS REÇUE
     * Non utilisé dans l'écran d'inscription.
     */
    @Override
    public void onUsersReceived(List<UserView> utilisateurs) {
    }

    /**
     * CALLBACK : MESSAGE REÇU
     * Non utilisé dans l'écran d'inscription.
     */
    @Override
    public void onMessageReceived(ChatMessageView message) {
    }

    /**
     * CALLBACK : HISTORIQUE DE CONVERSATION REÇU
     * Non utilisé dans l'écran d'inscription.
     */
    @Override
    public void onHistoryReceived(String partenaire, List<ChatMessageView> historique) {
    }

    /**
     * CALLBACK : CHANGEMENT DE STATUT D'UN UTILISATEUR
     * Non utilisé dans l'écran d'inscription.
     */
    @Override
    public void onUserStatus(String nomUtilisateur, String statut) {
    }

    /**
     * ANIMATION DE SECOUSSE POUR SIGNALER UNE ERREUR
     * 
     * Cette méthode crée une animation de secousse sur les éléments d'interface passés en paramètre.
     * Elle est utilisée pour attirer visuellement l'attention de l'utilisateur sur une erreur.
     * 
     * ANIMATION :
     * - Déplacement : -5px ↔ +5px
     * - Répétitions : 4 fois (8 mouvements)
     * - Durée : 50ms par mouvement
     * - Durée totale : 400ms (0.4 seconde)
     * 
     * UTILISATION DANS CET ÉCRAN :
     * - Champs vides lors de l'inscription
     * - Mots de passe non correspondants
     * - Nom d'utilisateur déjà existant
     * - Erreur de connexion au serveur
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

