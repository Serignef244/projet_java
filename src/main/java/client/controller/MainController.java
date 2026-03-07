package client.controller;

import javafx.application.Platform;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import client.AppContext;
import client.model.ChatMessageView;
import client.model.UserView;
import client.network.ServerEventListener;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.awt.Toolkit;

/**
 * ControleurPrincipal - Gère l'interface principale de messagerie.
 */
public class MainController implements ServerEventListener {
    private static final DateTimeFormatter FORMAT_HEURE = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private BorderPane panneauRacine;
    @FXML
    private Label labelIdentite;
    @FXML
    private Label labelBadgeRole;
    @FXML
    private Label labelRetour;
    @FXML
    private ListView<UserView> listeMembres;
    @FXML
    private ListView<ChatMessageView> listeMessages;
    @FXML
    private TextField champMessage;
    @FXML
    private Label labelUtilisateurSelectionne;
    @FXML
    private Label labelConnexion;
    @FXML
    private Button boutonReconnexion;
    @FXML
    private Label labelCompteMembres;
    @FXML
    private ListView<UserView> listeGestionMembres;
    @FXML
    private VBox panneauOrganisateur;
    @FXML
    private VBox panneauMembres;
    @FXML
    private Button boutonEnvoyer;
    @FXML
    private Label labelBanniereHorsLigne;
    @FXML
    private TextField champRecherche;
    @FXML
    private Label labelSaisie;
    @FXML
    private Button boutonBasculerMembres;

    private final List<ChatMessageView> conversationActuelle = new ArrayList<>();
    private final List<UserView> utilisateursVisibles = new ArrayList<>();
    private final AtomicBoolean enReconnexion = new AtomicBoolean(false);
    private String utilisateurSelectionne;
    private volatile boolean modeHorsLigne;
    private boolean membresReduits;
    private boolean roleOrganisateur;
    private boolean reductionForceMobile;
    private int dernierNombreMessagesAffiches;

    @FXML
    public void initialize() {
        // Initialisation de l'identité et du badge de rôle
        labelIdentite.setText(AppContext.loggedUser());
        appliquerBadgeRole(AppContext.loggedRole());

        // Panneau de gestion réservé aux organisateurs
        roleOrganisateur = "ORGANISATEUR".equalsIgnoreCase(AppContext.loggedRole());
        panneauOrganisateur.setManaged(roleOrganisateur);
        panneauOrganisateur.setVisible(roleOrganisateur);

        configurerListeMembres(listeMembres);
        configurerListeMembres(listeGestionMembres);
        configurerListeMessages();

        // Double-clic pour ouvrir une conversation
        listeMembres.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                UserView selectionne = listeMembres.getSelectionModel().getSelectedItem();
                ouvrirConversation(selectionne);
            }
        });
        listeGestionMembres.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                UserView selectionne = listeGestionMembres.getSelectionModel().getSelectedItem();
                ouvrirConversation(selectionne);
            }
        });

        champMessage.textProperty().addListener((obs, ancienneValeur, nouvelleValeur) -> {
            // Indicateur de saisie en cours
            labelSaisie.setText(nouvelleValeur != null && !nouvelleValeur.isBlank() ? "En train d'écrire..." : "");
        });

        // État initial : connexion active
        labelRetour.setText("Prêt.");
        labelConnexion.getStyleClass().setAll("status-pill", "status-online");
        labelConnexion.setText("EN LIGNE");
        boutonReconnexion.setVisible(false);
        boutonReconnexion.setManaged(false);
        labelBanniereHorsLigne.setVisible(false);
        labelBanniereHorsLigne.setManaged(false);

        // Adaptation responsive automatique
        panneauRacine.widthProperty().addListener((obs, ancienneValeur, nouvelleValeur) -> 
            appliquerMiseEnPageResponsive(nouvelleValeur.doubleValue()));
        Platform.runLater(() -> appliquerMiseEnPageResponsive(panneauRacine.getWidth()));
    }

    @FXML
    private void envoyerMessageAuDestinataire() {
        // Vérifications avant envoi
        if (utilisateurSelectionne == null || utilisateurSelectionne.isBlank()) {
            labelRetour.setText("Sélectionnez un destinataire.");
            return;
        }
        if (modeHorsLigne) {
            labelRetour.setText("Vous êtes hors ligne.");
            return;
        }
        String contenu = champMessage.getText();
        if (contenu == null || contenu.isBlank()) {
            labelRetour.setText("Message vide.");
            return;
        }
        if (contenu.length() > 1000) {
            labelRetour.setText("Message > 1000 caractères.");
            return;
        }
        AppContext.connection().sendMessage(utilisateurSelectionne, contenu);
        animerEnvoiMessage();
        champMessage.clear();
        labelSaisie.setText("");
    }

    @FXML
    private void rafraichirListeDesMembres() {
        // Mise à jour de la liste des membres
        if (!modeHorsLigne) {
            AppContext.connection().requestUsers();
        }
    }

    @FXML
    private void deconnecterUtilisateur() {
        // Déconnexion et retour à l'accueil
        if (AppContext.connection() != null) {
            AppContext.connection().logout();
        }
        AppContext.clearIdentity();
        try {
            AppContext.showWelcome();
        } catch (IOException e) {
            labelRetour.setText("Erreur de retour: " + e.getMessage());
        }
    }

    @FXML
    private void reconnecterAuServeur() {
        // Reconnexion manuelle
        tenterReconnexion(false);
    }

    @FXML
    private void rechercherDansListeMembres() {
        // Filtrage local de la liste
        appliquerFiltreMembres();
    }

    @FXML
    private void afficherOuMasquerPanneauMembres() {
        // Bascule affichage/masquage du panneau membres
        definirMembresReduits(!membresReduits);
    }

    @FXML
    private void basculerEntreThemeClairEtSombre() {
        // Bascule thème clair/sombre
        AppContext.toggleTheme();
    }

    @Override
    public void onUsersReceived(List<UserView> utilisateurs) {
        // Mise à jour de la liste en retirant l'utilisateur courant
        utilisateursVisibles.clear();
        utilisateursVisibles.addAll(utilisateurs.stream()
                .filter(u -> !u.username().equals(AppContext.loggedUser()))
                .toList());
        appliquerFiltreMembres();
    }

    @Override
    public void onMessageReceived(ChatMessageView message) {
        // Ouverture automatique de conversation si aucune n'est active
        if ((utilisateurSelectionne == null || utilisateurSelectionne.isBlank()) && 
            message.recipient().equals(AppContext.loggedUser())) {
            utilisateurSelectionne = message.sender();
            labelUtilisateurSelectionne.setText("Conversation avec " + utilisateurSelectionne);
            AppContext.connection().requestHistory(utilisateurSelectionne);
        }

        boolean appartientConversation = utilisateurSelectionne != null
                && ((message.sender().equals(AppContext.loggedUser()) && message.recipient().equals(utilisateurSelectionne))
                || (message.sender().equals(utilisateurSelectionne) && message.recipient().equals(AppContext.loggedUser())));
        if (appartientConversation) {
            // Message de la conversation active
            conversationActuelle.add(message);
            afficherConversation();
            return;
        }

        if (message.recipient().equals(AppContext.loggedUser())) {
            labelRetour.setText("Nouveau message de " + message.sender());
            notifierNouveauMessage(message);
        }
    }

    @Override
    public void onHistoryReceived(String partenaire, List<ChatMessageView> historique) {
        // Affichage de l'historique uniquement si c'est le bon partenaire
        if (utilisateurSelectionne != null && utilisateurSelectionne.equals(partenaire)) {
            conversationActuelle.clear();
            conversationActuelle.addAll(historique);
            afficherConversation();
        }
    }

    @Override
    public void onUserStatus(String nomUtilisateur, String statut) {
        // Notification de changement de statut
        labelRetour.setText("Statut: " + nomUtilisateur + " est " + statut);
        if (!modeHorsLigne) {
            AppContext.connection().requestUsers();
        }
    }

    @Override
    public void onError(String code, String message) {
        // Gestion des erreurs serveur
        labelRetour.setText(code + ": " + message);
    }

    @Override
    public void onInfo(String texte) {
        // Messages informatifs
        labelRetour.setText(texte);
    }

    @Override
    public void onDisconnected(String raison) {
        // Passage en mode hors ligne avec tentative de reconnexion
        definirModeHorsLigne(true, raison);
        tenterReconnexion(true);
    }

    @Override
    public void onLoginSuccess(String nomUtilisateur, String role) {
        // Retour en ligne après reconnexion
        definirModeHorsLigne(false, "Reconnecté.");
        AppContext.connection().setBufferIncomingMessages(false);
        AppContext.connection().requestUsers();
        if (utilisateurSelectionne != null && !utilisateurSelectionne.isBlank()) {
            AppContext.connection().requestHistory(utilisateurSelectionne);
        }
    }

    @Override
    public void onRegisterSuccess(String nomUtilisateur) {
        // Non utilisé dans l'écran principal
    }

    private void ouvrirConversation(UserView selectionne) {
        if (selectionne == null) {
            return;
        }
        // Ouverture de conversation et récupération de l'historique
        utilisateurSelectionne = selectionne.username();
        labelUtilisateurSelectionne.setText("Conversation avec " + utilisateurSelectionne);
        AppContext.connection().requestHistory(utilisateurSelectionne);
    }

    private void afficherConversation() {
        // Tri chronologique et affichage des messages
        conversationActuelle.sort(Comparator.comparing(ChatMessageView::timestamp));
        listeMessages.getItems().setAll(conversationActuelle);
        if (conversationActuelle.size() > dernierNombreMessagesAffiches) {
            animerApparitionDernierMessage();
        }
        dernierNombreMessagesAffiches = conversationActuelle.size();
        listeMessages.scrollTo(listeMessages.getItems().size() - 1);
    }

    private void appliquerFiltreMembres() {
        // Filtrage local de la liste des membres
        String requete = champRecherche.getText() == null ? "" : champRecherche.getText().trim().toLowerCase();
        List<UserView> filtres = utilisateursVisibles.stream()
                .filter(u -> requete.isBlank() || u.username().toLowerCase().contains(requete))
                .toList();
        listeMembres.getItems().setAll(filtres);
        listeGestionMembres.getItems().setAll(filtres);
        labelCompteMembres.setText("Membres (" + filtres.size() + ")");
    }

    private void definirModeHorsLigne(boolean horsLigne, String raison) {
        // Gestion de l'état hors ligne
        this.modeHorsLigne = horsLigne;
        champMessage.setDisable(horsLigne);
        boutonEnvoyer.setDisable(horsLigne);
        listeMembres.setDisable(horsLigne);
        champRecherche.setDisable(horsLigne);
        if (horsLigne) {
            labelConnexion.getStyleClass().setAll("status-pill", "status-offline");
            labelConnexion.setText("HORS LIGNE");
            labelRetour.setText(raison);
            boutonReconnexion.setVisible(true);
            boutonReconnexion.setManaged(true);
            labelBanniereHorsLigne.setText("Serveur indisponible. Reconnexion...");
            labelBanniereHorsLigne.setVisible(true);
            labelBanniereHorsLigne.setManaged(true);
        } else {
            labelConnexion.getStyleClass().setAll("status-pill", "status-online");
            labelConnexion.setText("EN LIGNE");
            labelRetour.setText(raison);
            boutonReconnexion.setVisible(false);
            boutonReconnexion.setManaged(false);
            labelBanniereHorsLigne.setVisible(false);
            labelBanniereHorsLigne.setManaged(false);
        }
    }

    private void tenterReconnexion(boolean automatique) {
        if (!modeHorsLigne || !enReconnexion.compareAndSet(false, true)) {
            return;
        }
        // Reconnexion asynchrone
        new Thread(() -> {
            try {
                if (automatique) {
                    Thread.sleep(3000);
                }
                String nomUtilisateur = AppContext.authUsername();
                String motDePasse = AppContext.authPassword();
                if (nomUtilisateur == null || motDePasse == null) {
                    enReconnexion.set(false);
                    return;
                }
                var connexion = new client.network.ServerConnection("localhost", 8088, this);
                connexion.connect();
                connexion.setBufferIncomingMessages(false);
                connexion.login(nomUtilisateur, motDePasse);
                AppContext.setConnection(connexion);
            } catch (Exception e) {
                Platform.runLater(() -> labelRetour.setText("Reconnexion échouée: " + e.getMessage()));
            } finally {
                enReconnexion.set(false);
            }
        }).start();
    }

    private void configurerListeMembres(ListView<UserView> listeVue) {
        // Configuration personnalisée des cellules de la liste
        listeVue.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(UserView item, boolean vide) {
                super.updateItem(item, vide);
                if (vide || item == null) {
                    setGraphic(null);
                    return;
                }
                // Avatar avec initiales
                Label avatar = new Label(obtenirInitiales(item.username()));
                avatar.getStyleClass().add("avatar");

                // Informations utilisateur
                Label nom = new Label(item.username());
                Label role = new Label(item.role());
                role.getStyleClass().add("caption-text");
                VBox boiteTexte = new VBox(nom, role);
                boiteTexte.setSpacing(2);

                Label statut = new Label("ONLINE".equalsIgnoreCase(item.status()) ? "ONLINE" : "OFFLINE");
                statut.getStyleClass().addAll("status-pill",
                        "ONLINE".equalsIgnoreCase(item.status()) ? "status-online" : "status-offline");

                Region espaceur = new Region();
                HBox.setHgrow(espaceur, Priority.ALWAYS);

                HBox ligne = new HBox(8, avatar, boiteTexte, espaceur, statut);
                ligne.setAlignment(Pos.CENTER_LEFT);
                ligne.getStyleClass().add("member-row");
                setGraphic(ligne);
            }
        });
    }

    private void configurerListeMessages() {
        // Configuration personnalisée des bulles de messages
        listeMessages.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ChatMessageView item, boolean vide) {
                super.updateItem(item, vide);
                if (vide || item == null) {
                    setGraphic(null);
                    return;
                }
                boolean mien = item.sender().equals(AppContext.loggedUser());

                Label contenu = new Label(item.content());
                contenu.setWrapText(true);
                contenu.getStyleClass().add(mien ? "message-text-mine" : "message-text-other");

                Label meta = new Label((mien ? "Moi" : item.sender()) + " - " + FORMAT_HEURE.format(item.timestamp()));
                meta.getStyleClass().add("caption-text");

                VBox bulle = new VBox(4, contenu, meta);
                bulle.getStyleClass().add(mien ? "message-bubble-mine" : "message-bubble-other");
                bulle.setMaxWidth(430);

                Region espaceur = new Region();
                HBox.setHgrow(espaceur, Priority.ALWAYS);
                HBox conteneur = mien ? new HBox(espaceur, bulle) : new HBox(bulle, espaceur);
                conteneur.setAlignment(Pos.CENTER_LEFT);
                conteneur.setPadding(new javafx.geometry.Insets(4, 6, 4, 6));

                setGraphic(conteneur);
            }
        });
    }

    private void notifierNouveauMessage(ChatMessageView message) {
        // Notification non bloquante + son d'arrivee.
        jouerNotificationSonore();
        animerInfoReception();
    }

    private void appliquerBadgeRole(String role) {
        // Application du style de badge selon le rôle
        labelBadgeRole.setText(role);
        labelBadgeRole.getStyleClass().removeAll("role-organisateur", "role-membre", "role-benevole");
        if ("ORGANISATEUR".equalsIgnoreCase(role)) {
            labelBadgeRole.getStyleClass().add("role-organisateur");
        } else if ("BENEVOLE".equalsIgnoreCase(role)) {
            labelBadgeRole.getStyleClass().add("role-benevole");
        } else {
            labelBadgeRole.getStyleClass().add("role-membre");
        }
    }

    private void appliquerMiseEnPageResponsive(double largeur) {
        // Adaptation responsive selon la largeur
        panneauRacine.getStyleClass().removeAll("phone-mode", "tablet-mode", "desktop-mode");
        if (largeur < 760) {
            panneauRacine.getStyleClass().add("phone-mode");
            if (!reductionForceMobile) {
                reductionForceMobile = true;
                definirMembresReduits(true);
            }
        } else if (largeur < 1100) {
            panneauRacine.getStyleClass().add("tablet-mode");
            if (reductionForceMobile) {
                reductionForceMobile = false;
                definirMembresReduits(false);
            }
        } else {
            panneauRacine.getStyleClass().add("desktop-mode");
            if (reductionForceMobile) {
                reductionForceMobile = false;
                definirMembresReduits(false);
            }
        }

        boolean afficherPanneauOrganisateur = roleOrganisateur && largeur >= 980;
        panneauOrganisateur.setVisible(afficherPanneauOrganisateur);
        panneauOrganisateur.setManaged(afficherPanneauOrganisateur);

        if (!membresReduits) {
            panneauMembres.setPrefWidth(largeur < 1100 ? 230 : 290);
        }
    }

    private void definirMembresReduits(boolean reduit) {
        // Gestion de l'état réduit/étendu du panneau membres
        membresReduits = reduit;
        boolean etendu = !reduit;

        champRecherche.setVisible(etendu);
        champRecherche.setManaged(etendu);
        listeMembres.setVisible(etendu);
        listeMembres.setManaged(etendu);
        labelRetour.setVisible(etendu);
        labelRetour.setManaged(etendu);
        labelCompteMembres.setVisible(etendu);
        labelCompteMembres.setManaged(etendu);

        panneauMembres.setPrefWidth(etendu ? 280 : 88);
        boutonBasculerMembres.setText(etendu ? "Masquer" : "Afficher");
    }

    private String obtenirInitiales(String nomUtilisateur) {
        // Extraction des initiales du nom d'utilisateur
        if (nomUtilisateur == null || nomUtilisateur.isBlank()) {
            return "?";
        }
        String[] morceaux = nomUtilisateur.split("[._\\-\\s]+");
        if (morceaux.length >= 2) {
            return (morceaux[0].substring(0, 1) + morceaux[1].substring(0, 1)).toUpperCase();
        }
        String valeur = morceaux[0];
        return valeur.length() >= 2 ? valeur.substring(0, 2).toUpperCase() : valeur.toUpperCase();
    }

    private void animerEnvoiMessage() {
        ScaleTransition transition = new ScaleTransition(Duration.millis(120), boutonEnvoyer);
        transition.setFromX(1.0);
        transition.setFromY(1.0);
        transition.setToX(0.94);
        transition.setToY(0.94);
        transition.setCycleCount(2);
        transition.setAutoReverse(true);
        transition.play();
    }

    private void animerApparitionDernierMessage() {
        Platform.runLater(() -> {
            int dernierIndex = listeMessages.getItems().size() - 1;
            if (dernierIndex < 0) {
                return;
            }
            for (Node node : listeMessages.lookupAll(".list-cell")) {
                if (node instanceof ListCell<?> cell && cell.getIndex() == dernierIndex) {
                    Node cible = cell.getGraphic() != null ? cell.getGraphic() : cell;
                    cible.setOpacity(0);
                    cible.setTranslateY(10);

                    FadeTransition fade = new FadeTransition(Duration.millis(220), cible);
                    fade.setFromValue(0);
                    fade.setToValue(1);

                    TranslateTransition slide = new TranslateTransition(Duration.millis(220), cible);
                    slide.setFromY(10);
                    slide.setToY(0);

                    new ParallelTransition(fade, slide).play();
                    break;
                }
            }
        });
    }

    private void animerInfoReception() {
        ScaleTransition transition = new ScaleTransition(Duration.millis(150), labelRetour);
        transition.setFromX(1.0);
        transition.setFromY(1.0);
        transition.setToX(1.04);
        transition.setToY(1.04);
        transition.setCycleCount(2);
        transition.setAutoReverse(true);
        transition.play();
    }

    private void jouerNotificationSonore() {
        try {
            Toolkit.getDefaultToolkit().beep();
        } catch (Throwable ignored) {
            // Ignored on platforms where system beep is unavailable.
        }
    }
}

