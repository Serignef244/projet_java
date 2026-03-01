package service;

import dao.MessageRepository;
import dao.UserRepository;

/**
 * ServerServices - composant principal de l'application.
 */
public class ServerServices {
    private final SessionRegistry sessionRegistry;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final AuthService authService;
    private final MessageService messageService;

    public ServerServices(SessionRegistry sessionRegistry, UserRepository userRepository, MessageRepository messageRepository) {
        this.sessionRegistry = sessionRegistry;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.messageService = new MessageService(messageRepository, userRepository);
        this.authService = new AuthService(userRepository, messageService, sessionRegistry);
    }

    public SessionRegistry sessionRegistry() {
        return sessionRegistry;
    }

    public UserRepository userRepository() {
        return userRepository;
    }

    public MessageRepository messageRepository() {
        return messageRepository;
    }

    public AuthService authService() {
        return authService;
    }

    public MessageService messageService() {
        return messageService;
    }
}

