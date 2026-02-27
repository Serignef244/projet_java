package config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * JpaUtil - composant principal de l'application.
 */
public final class JpaUtil {
    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory("messageriePU");

    private JpaUtil() {
    }

    public static EntityManagerFactory emf() {
        return EMF;
    }

    public static void shutdown() {
        if (EMF.isOpen()) {
            EMF.close();
        }
    }
}

