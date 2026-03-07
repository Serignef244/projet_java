package client.controller;

import javafx.fxml.FXML;
import client.AppContext;

import java.io.IOException;

/**
 * ControleurAccueil - Gère l'écran d'accueil de l'application.
 * 
 * CET ÉCRAN EST LE POINT D'ENTRÉE DE L'APPLICATION.
 * Il permet à l'utilisateur de choisir entre :
 * - Se connecter (si compte existant)
 * - S'inscrire (si nouveau)
 * - Changer le thème (clair/sombre)
 */
public class WelcomeController {
    /**
     * NAVIGATION VERS L'ÉCRAN DE CONNEXION
     * 
     * Cette méthode est déclenchée quand l'utilisateur clique sur "SE CONNECTER".
     * Elle charge et affiche l'écran de connexion (login.fxml).
     * 
     * Utilisation typique :
     * - L'utilisateur a déjà un compte
     * - L'utilisateur veut se connecter à la messagerie
     * 
     * Fonctionnement :
     * 1. Demande à AppContext de charger login.fxml
     * 2. Affiche l'écran de connexion
     * 3. Les erreurs de chargement sont ignorées (non critiques)
     */
    @FXML
    private void allerVersEcranConnexion() {
        // Navigation vers l'écran de connexion
        try {
            AppContext.showLogin();
        } catch (IOException ignored) {
        }
    }

    /**
     * NAVIGATION VERS L'ÉCRAN D'INSCRIPTION
     * 
     * Cette méthode est déclenchée quand l'utilisateur clique sur "S'INSCRIRE".
     * Elle charge et affiche l'écran d'inscription (register.fxml).
     * 
     * Utilisation typique :
     * - L'utilisateur n'a pas encore de compte
     * - L'utilisateur veut créer un nouveau compte
     * 
     * Fonctionnement :
     * 1. Demande à AppContext de charger register.fxml
     * 2. Affiche l'écran d'inscription
     * 3. Les erreurs de chargement sont ignorées (non critiques)
     */
    @FXML
    private void allerVersEcranInscription() {
        // Navigation vers l'écran d'inscription
        try {
            AppContext.showRegister();
        } catch (IOException ignored) {
        }
    }

    /**
     * BASCULER ENTRE THÈME CLAIR ET SOMBRE
     * 
     * Cette méthode est déclenchée quand l'utilisateur clique sur "Mode sombre".
     * Elle bascule l'apparence de l'application entre le thème clair et le thème sombre.
     * 
     * Fonctionnement :
     * 1. Demande à AppContext de basculer le thème
     * 2. Le changement est appliqué immédiatement à toute l'interface
     * 3. Le choix est mémorisé pour les prochains écrans
     * 
     * THÈMES DISPONIBLES :
     * - Clair : Fond blanc, texte noir, parfait pour le jour
     * - Sombre : Fond noir, texte blanc, parfait pour la nuit
     */
    @FXML
    private void basculerEntreThemeClairEtSombre() {
        // Bascule entre thème clair et sombre
        AppContext.toggleTheme();
    }
}

