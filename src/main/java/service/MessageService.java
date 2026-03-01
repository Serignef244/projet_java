package service;

import dao.MessageRepository;
import dao.UserRepository;
import model.Message;
import model.MessageStatus;
import model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * MessageService - composant principal de l'application.
 */
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public Optional<User> findUser(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<Message> createMessage(String senderUsername, String recipientUsername, String content, boolean recipientOnline) {
        // RG5/RG7: expéditeur et destinataire doivent exister + contrôle contenu.
        if (content == null || content.isBlank() || content.length() > 1000) {
            return Optional.empty();
        }
        Optional<User> sender = userRepository.findByUsername(senderUsername);
        Optional<User> recipient = userRepository.findByUsername(recipientUsername);
        if (sender.isEmpty() || recipient.isEmpty()) {
            return Optional.empty();
        }
        Message message = new Message();
        message.setSender(sender.get());
        message.setRecipient(recipient.get());
        message.setContent(content.trim());
        message.setTimestamp(LocalDateTime.now());
        // RG6: online => RECU, offline => ENVOYE puis livraison différée.
        message.setStatus(recipientOnline ? MessageStatus.RECU : MessageStatus.ENVOYE);
        return Optional.of(messageRepository.save(message));
    }

    public List<Message> history(String usernameA, String usernameB) {
        // RG8: l'historique est renvoyé trié chronologiquement.
        return messageRepository.findConversation(usernameA, usernameB);
    }

    public List<Message> pendingFor(String username) {
        return messageRepository.findPendingForRecipient(username);
    }

    public void markReceived(List<Message> messages) {
        List<Long> ids = messages.stream().map(Message::getId).toList();
        messageRepository.markReceived(ids);
    }

    public void markReadConversation(String recipientUsername, String senderUsername) {
        messageRepository.markReadForConversation(recipientUsername, senderUsername);
    }

    public List<User> conversationPartners(String username) {
        return messageRepository.findConversationPartners(username);
    }
}

