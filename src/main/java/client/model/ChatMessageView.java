package client.model;

import java.time.LocalDateTime;

/**
 * ChatMessageView - composant principal de l'application.
 */
public record ChatMessageView(String sender, String recipient, LocalDateTime timestamp, String content) {
    // Record immutable utilise par l'UI pour afficher un message.
    // sender: auteur du message.
    // recipient: destinataire du message.
    // timestamp: horodatage ISO recu du serveur.
    // content: texte decode (UTF-8) a afficher dans la bulle.
}

