package client.model;

/**
 * UserView - composant principal de l'application.
 */
public record UserView(String username, String status, String role) {
    @Override
    public String toString() {
        // Representation texte de secours (debug/logs).
        return username + " (" + status + ", " + role + ")";
    }
}

