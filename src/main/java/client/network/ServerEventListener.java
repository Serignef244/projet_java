package client.network;

import client.model.ChatMessageView;
import client.model.UserView;

import java.util.List;

/**
 * ServerEventListener - composant principal de l'application.
 */
public interface ServerEventListener {
    // Callback appele quand LOGIN est valide.
    void onLoginSuccess(String username, String role);

    // Callback appele quand REGISTER est valide.
    void onRegisterSuccess(String username);

    // Retour liste des utilisateurs visibles.
    void onUsersReceived(List<UserView> users);

    // Nouveau message instantane recu via socket.
    void onMessageReceived(ChatMessageView message);

    // Historique de conversation charge a la demande.
    void onHistoryReceived(String partner, List<ChatMessageView> history);

    // Notification de changement de statut d'un utilisateur.
    void onUserStatus(String username, String status);

    // Message informatif non bloquant.
    void onInfo(String text);

    // Erreur protocolaire/metier retournee par le serveur.
    void onError(String code, String message);

    // Rupture de connexion ou fermeture distante.
    void onDisconnected(String reason);
}

