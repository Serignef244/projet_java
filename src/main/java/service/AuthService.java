package service;

import dao.UserRepository;
import model.Role;
import model.Status;
import model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AuthService - composant principal de l'application.
 */
public class AuthService {
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final SessionRegistry sessions;

    public AuthService(UserRepository userRepository, MessageService messageService, SessionRegistry sessions) {
        this.userRepository = userRepository;
        this.messageService = messageService;
        this.sessions = sessions;
    }

    public Optional<User> register(String username, String rawPassword, Role role) {
        // RG1/RG9: username unique + mot de passe stocké hashé (jamais en clair).
        if (username == null || username.isBlank() || rawPassword == null || rawPassword.isBlank() || role == null) {
            return Optional.empty();
        }
        if (userRepository.findByUsername(username).isPresent()) {
            return Optional.empty();
        }
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
        user.setRole(role);
        user.setStatus(Status.OFFLINE);
        user.setDateCreation(LocalDateTime.now());
        return Optional.of(userRepository.save(user));
    }

    public Optional<User> authenticate(String username, String rawPassword) {
        // RG2/RG9: vérifie les identifiants via BCrypt.
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        User user = userOpt.get();
        if (!BCrypt.checkpw(rawPassword, user.getPassword())) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    public boolean canLogin(User user) {
        // RG3: bloque toute double connexion du même utilisateur.
        return user.getStatus() != Status.ONLINE && !sessions.isOnline(user.getUsername());
    }

    public void markOnline(String username) {
        userRepository.updateStatus(username, Status.ONLINE);
    }

    public void markOffline(String username) {
        userRepository.updateStatus(username, Status.OFFLINE);
    }

    public List<User> listVisibleUsers(User currentUser) {
        // RG13: un organisateur voit l'annuaire complet.
        if (currentUser.getRole() == Role.ORGANISATEUR) {
            return userRepository.findAllOrderByUsername();
        }
        List<User> online = userRepository.findOnlineOrderByUsername();
        List<User> partners = messageService.conversationPartners(currentUser.getUsername());

        Map<String, User> merged = new LinkedHashMap<>();
        for (User user : online) {
            merged.put(user.getUsername(), user);
        }
        for (User user : partners) {
            if (!user.getUsername().equals(currentUser.getUsername())) {
                merged.putIfAbsent(user.getUsername(), user);
            }
        }
        return merged.values().stream()
                .sorted(Comparator.comparing(User::getUsername))
                .toList();
    }
}

