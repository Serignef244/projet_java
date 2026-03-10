package server;

import config.JpaUtil;
import jakarta.persistence.EntityManager;

/**
 * TestDB - composant principal de l'application.
 */
public class TestDB {
    public static void main(String[] args) {
        // Petit utilitaire de verification de la connexion JPA.
        System.out.println("Test de connexion a la base de donnees...");
        try {
            // Ouverture d'un EntityManager pour valider la configuration persistence.xml.
            EntityManager em = JpaUtil.emf().createEntityManager();
            System.out.println("Connexion reussie a la base de donnees !");
            System.out.println("Les tables seront creees automatiquement par Hibernate");
            em.close();
        } catch (Exception e) {
            // Affiche l'erreur exacte pour diagnostic local.
            System.err.println("Erreur de connexion : " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Fermeture propre de l'EMF.
            JpaUtil.shutdown();
        }
    }
}

