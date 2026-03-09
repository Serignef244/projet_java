# 📚 DOCUMENTATION COMPLÈTE DES FONCTIONS

## 🎯 Vue d'Ensemble

Ce document décrit **TOUTES LES FONCTIONS** de l'application de messagerie, organisées par fichier.

---

## 📁 CLIENT - CONTRÔLEURS

### 🔐 LoginController.java

#### **initialize()**
- **Rôle** : Initialise l'écran de connexion
- **Quand** : Appelée automatiquement par JavaFX après chargement du FXML
- **Actions** :
  1. Récupère un éventuel message flash (après inscription)
  2. Affiche ce message ou "Serveur: localhost:8088"

#### **surConnexion()**
- **Rôle** : Gère le clic sur "SE CONNECTER"
- **Quand** : Utilisateur clique sur le bouton
- **Actions** :
  1. Récupère nom d'utilisateur et mot de passe
  2. Vérifie que les champs ne sont pas vides
  3. Si vides → affiche erreur + secoue les champs
  4. Si valides → établit connexion serveur
  5. Envoie identifiants pour authentification

#### **surAllerInscription()**
- **Rôle** : Navigation vers l'écran d'inscription
- **Quand** : Utilisateur clique sur "S'INSCRIRE"
- **Actions** : Charge et affiche register.fxml

#### **surRetour()**
- **Rôle** : Retour à l'écran d'accueil
- **Quand** : Utilisateur clique sur "RETOUR"
- **Actions** : Charge et affiche welcome.fxml

#### **surBasculerTheme()**
- **Rôle** : Bascule entre thème clair et sombre
- **Quand** : Utilisateur clique sur "Mode sombre"
- **Actions** : Change le thème de toute l'application

#### **assurerConnexion()**
- **Rôle** : Établit ou réutilise une connexion serveur
- **Quand** : Avant d'envoyer une requête au serveur
- **Actions** :
  1. Vérifie si connexion existe
  2. Si non → crée nouvelle connexion socket (localhost:8088)
  3. Si oui → réutilise connexion existante
- **Retour** : ServerConnection

#### **onLoginSuccess(nomUtilisateur, role)**
- **Rôle** : Callback appelé quand connexion réussit
- **Quand** : Serveur valide les identifiants
- **Actions** :
  1. Stocke identité utilisateur
  2. Affiche message de succès
  3. Charge écran principal (main.fxml)
  4. Configure réception événements réseau
  5. Demande liste des utilisateurs

#### **onRegisterSuccess(nomUtilisateur)**
- **Rôle** : Callback appelé quand inscription réussit
- **Quand** : Serveur confirme l'inscription
- **Actions** :
  1. Crée message flash de succès
  2. Redirige vers écran de connexion

#### **onError(code, message)**
- **Rôle** : Callback appelé en cas d'erreur serveur
- **Quand** : Serveur renvoie une erreur
- **Exemples** : AUTH_FAILED, USER_EXISTS, INVALID_REQUEST
- **Actions** :
  1. Affiche code + message d'erreur
  2. Secoue les champs

#### **onInfo(texte)**
- **Rôle** : Callback pour messages informatifs
- **Quand** : Serveur envoie une info
- **Actions** : Affiche le message

#### **onDisconnected(raison)**
- **Rôle** : Callback appelé en cas de déconnexion
- **Quand** : Connexion au serveur perdue
- **Actions** : Affiche la raison

#### **onUsersReceived(utilisateurs)**
- **Rôle** : Callback pour liste des utilisateurs
- **Utilisation** : Non utilisé dans cet écran

#### **onMessageReceived(message)**
- **Rôle** : Callback pour message reçu
- **Utilisation** : Non utilisé dans cet écran

#### **onHistoryReceived(partenaire, historique)**
- **Rôle** : Callback pour historique conversation
- **Utilisation** : Non utilisé dans cet écran

#### **onUserStatus(nomUtilisateur, statut)**
- **Rôle** : Callback pour changement de statut
- **Utilisation** : Non utilisé dans cet écran

#### **secouer(noeuds...)**
- **Rôle** : Animation de secousse pour signaler erreur
- **Quand** : Erreur de validation ou serveur
- **Animation** :
  - Déplacement : -5px ↔ +5px
  - Répétitions : 4 fois
  - Durée : 50ms par mouvement
  - Total : 400ms

---

### 📝 RegisterController.java

#### **initialize()**
- **Rôle** : Initialise l'écran d'inscription
- **Quand** : Appelée automatiquement par JavaFX
- **Actions** :
  1. Ajoute les 3 rôles : ORGANISATEUR, MEMBRE, BENEVOLE
  2. Sélectionne "MEMBRE" par défaut
  3. Affiche message d'instruction

#### **surSoumettreInscription()**
- **Rôle** : Gère le clic sur "VALIDER L'INSCRIPTION"
- **Quand** : Utilisateur soumet le formulaire
- **Validations** :
  1. Nom d'utilisateur non vide
  2. Mot de passe non vide
  3. Mots de passe correspondants
- **Actions si OK** :
  1. Établit connexion serveur
  2. Envoie demande d'inscription (nom, mdp, rôle)
- **Actions si erreur** :
  1. Affiche message d'erreur
  2. Secoue les champs

#### **surRetourConnexion()**
- **Rôle** : Retour à l'écran de connexion
- **Quand** : Utilisateur clique sur "RETOUR CONNEXION"
- **Actions** : Charge login.fxml

#### **surBasculerTheme()**
- **Rôle** : Bascule thème clair/sombre
- **Quand** : Utilisateur clique sur "Mode sombre"
- **Actions** : Change le thème

#### **assurerConnexion()**
- **Rôle** : Établit ou réutilise connexion serveur
- **Fonctionnement** : Identique à LoginController

#### **onRegisterSuccess(nomUtilisateur)**
- **Rôle** : Callback inscription réussie
- **Actions** :
  1. Crée message flash
  2. Redirige vers connexion

#### **onError(code, message)**
- **Rôle** : Callback erreur serveur
- **Erreurs possibles** :
  - USER_EXISTS : Nom déjà pris
  - INVALID_USERNAME : Nom invalide
  - WEAK_PASSWORD : Mot de passe faible
  - INVALID_ROLE : Rôle non reconnu

#### **Autres callbacks**
- Identiques à LoginController (non utilisés ici)

#### **secouer(noeuds...)**
- **Rôle** : Animation de secousse
- **Utilisation** :
  - Champs vides
  - Mots de passe non correspondants
  - Nom déjà existant
  - Erreur serveur

---

### 🏠 WelcomeController.java

#### **surAllerConnexion()**
- **Rôle** : Navigation vers connexion
- **Quand** : Utilisateur clique sur "SE CONNECTER"
- **Actions** : Charge login.fxml

#### **surAllerInscription()**
- **Rôle** : Navigation vers inscription
- **Quand** : Utilisateur clique sur "S'INSCRIRE"
- **Actions** : Charge register.fxml

#### **surBasculerTheme()**
- **Rôle** : Bascule thème
- **Quand** : Utilisateur clique sur "Mode sombre"
- **Actions** : Change le thème

---

### 💬 MainController.java (ÉCRAN PRINCIPAL)

#### **initialize()**
- **Rôle** : Initialise l'interface principale de messagerie
- **Quand** : Appelée automatiquement après chargement
- **Actions** :
  1. Affiche identité utilisateur (nom + rôle)
  2. Applique badge de rôle (couleur selon rôle)
  3. Configure panneau organisateur (si rôle = ORGANISATEUR)
  4. Configure listes (membres, messages)
  5. Configure double-clic pour ouvrir conversation
  6. Configure indicateur de saisie
  7. Initialise état connexion (EN LIGNE)
  8. Configure adaptation responsive

#### **surEnvoyerMessage()**
- **Rôle** : Envoie un message
- **Quand** : Utilisateur clique sur "Envoyer"
- **Vérifications** :
  1. Destinataire sélectionné
  2. Connexion active (pas hors ligne)
  3. Message non vide
  4. Message < 1000 caractères
- **Actions si OK** :
  1. Envoie message au serveur
  2. Vide le champ de saisie
  3. Efface indicateur de saisie

#### **surRafraichirMembres()**
- **Rôle** : Rafraîchit la liste des membres
- **Quand** : Utilisateur clique sur "Rafraîchir"
- **Actions** : Demande liste mise à jour au serveur

#### **surDeconnexion()**
- **Rôle** : Déconnexion de l'application
- **Quand** : Utilisateur clique sur "Déconnexion"
- **Actions** :
  1. Envoie commande LOGOUT au serveur
  2. Efface identité locale
  3. Retourne à l'écran d'accueil

#### **surReconnexion()**
- **Rôle** : Reconnexion manuelle
- **Quand** : Utilisateur clique sur "Reconnexion" (mode hors ligne)
- **Actions** : Lance tentative de reconnexion

#### **surRechercherMembres()**
- **Rôle** : Filtre la liste des membres
- **Quand** : Utilisateur tape dans la barre de recherche
- **Actions** : Filtre localement par nom

#### **surBasculerPanneauMembres()**
- **Rôle** : Affiche/masque le panneau membres
- **Quand** : Utilisateur clique sur "Masquer"/"Afficher"
- **Actions** : Bascule état réduit/étendu

#### **surBasculerTheme()**
- **Rôle** : Bascule thème
- **Actions** : Change le thème

#### **onUsersReceived(utilisateurs)**
- **Rôle** : Callback liste utilisateurs reçue
- **Quand** : Serveur envoie la liste
- **Actions** :
  1. Met à jour liste locale
  2. Retire utilisateur courant
  3. Applique filtre de recherche

#### **onMessageReceived(message)**
- **Rôle** : Callback message reçu
- **Quand** : Serveur envoie un nouveau message
- **Actions** :
  1. Si aucune conversation ouverte → ouvre avec expéditeur
  2. Si message de conversation active → affiche immédiatement
  3. Sinon → affiche notification

#### **onHistoryReceived(partenaire, historique)**
- **Rôle** : Callback historique reçu
- **Quand** : Serveur envoie historique conversation
- **Actions** :
  1. Vérifie que c'est le bon partenaire
  2. Remplace conversation locale
  3. Affiche les messages

#### **onUserStatus(nomUtilisateur, statut)**
- **Rôle** : Callback changement de statut
- **Quand** : Utilisateur passe ONLINE/OFFLINE
- **Actions** :
  1. Affiche notification
  2. Rafraîchit liste membres

#### **onError(code, message)**
- **Rôle** : Callback erreur serveur
- **Actions** : Affiche l'erreur

#### **onInfo(texte)**
- **Rôle** : Callback message informatif
- **Actions** : Affiche le message

#### **onDisconnected(raison)**
- **Rôle** : Callback déconnexion
- **Actions** :
  1. Passe en mode hors ligne
  2. Lance reconnexion automatique

#### **onLoginSuccess(nomUtilisateur, role)**
- **Rôle** : Callback reconnexion réussie
- **Actions** :
  1. Repasse en mode en ligne
  2. Désactive buffer messages
  3. Demande liste utilisateurs
  4. Restaure conversation si ouverte

#### **ouvrirConversation(selectionne)**
- **Rôle** : Ouvre une conversation
- **Quand** : Double-clic sur un membre
- **Actions** :
  1. Définit utilisateur sélectionné
  2. Met à jour titre conversation
  3. Demande historique au serveur

#### **afficherConversation()**
- **Rôle** : Affiche les messages de la conversation
- **Actions** :
  1. Trie messages par date
  2. Injecte dans la liste
  3. Scroll vers le dernier message

#### **appliquerFiltreMembres()**
- **Rôle** : Filtre la liste des membres
- **Actions** :
  1. Récupère texte de recherche
  2. Filtre par nom (contient)
  3. Met à jour les listes
  4. Met à jour compteur

#### **definirModeHorsLigne(horsLigne, raison)**
- **Rôle** : Gère l'état hors ligne
- **Actions** :
  1. Mémorise état
  2. Désactive/active champs
  3. Change statut visuel
  4. Affiche/masque bannière
  5. Affiche/masque bouton reconnexion

#### **tenterReconnexion(automatique)**
- **Rôle** : Tente de se reconnecter
- **Quand** : Automatique (après 3s) ou manuel
- **Actions** :
  1. Attend 3s si automatique
  2. Récupère identifiants mémorisés
  3. Crée nouvelle connexion
  4. Envoie LOGIN
  5. Met à jour AppContext

#### **configurerListeMembres(listeVue)**
- **Rôle** : Configure l'affichage des membres
- **Actions** :
  1. Crée cellule personnalisée avec :
     - Avatar (initiales)
     - Nom
     - Rôle
     - Statut (EN LIGNE/HORS LIGNE)

#### **configurerListeMessages()**
- **Rôle** : Configure l'affichage des messages
- **Actions** :
  1. Crée bulle personnalisée avec :
     - Contenu du message
     - Métadonnées (expéditeur + heure)
     - Style selon émetteur (bleu/gris)
     - Alignement (droite/gauche)

#### **notifierNouveauMessage(message)**
- **Rôle** : Affiche notification nouveau message
- **Actions** : Ouvre popup JavaFX Alert

#### **appliquerBadgeRole(role)**
- **Rôle** : Applique style badge selon rôle
- **Actions** :
  1. Affiche texte du rôle
  2. Applique classe CSS :
     - role-organisateur (doré)
     - role-membre (bleu)
     - role-benevole (vert)

#### **appliquerMiseEnPageResponsive(largeur)**
- **Rôle** : Adapte interface selon taille fenêtre
- **Paliers** :
  - < 760px : Mode mobile (panneau membres masqué)
  - 760-1100px : Mode tablette (panneaux réduits)
  - > 1100px : Mode desktop (tous panneaux visibles)
- **Actions** :
  1. Applique classe CSS (phone/tablet/desktop-mode)
  2. Ajuste visibilité panneau organisateur
  3. Ajuste largeur panneau membres

#### **definirMembresReduits(reduit)**
- **Rôle** : Réduit/étend panneau membres
- **Actions** :
  1. Affiche/masque éléments
  2. Ajuste largeur (280px ↔ 88px)
  3. Change texte bouton

#### **obtenirInitiales(nomUtilisateur)**
- **Rôle** : Extrait initiales pour avatar
- **Logique** :
  1. Si nom composé (point, tiret, espace) → 2 premières lettres
  2. Sinon → 2 premiers caractères
- **Retour** : String (ex: "JD", "AB")

---

## 📊 RÉSUMÉ PAR CONTRÔLEUR

```
┌──────────────────────┬──────────┬─────────────┬──────────────┐
│ Contrôleur           │ Méthodes │ Callbacks   │ Utilitaires  │
├──────────────────────┼──────────┼─────────────┼──────────────┤
│ LoginController      │ 5        │ 9           │ 2            │
│ RegisterController   │ 4        │ 9           │ 2            │
│ WelcomeController    │ 3        │ 0           │ 0            │
│ MainController       │ 10       │ 9           │ 9            │
├──────────────────────┼──────────┼─────────────┼──────────────┤
│ TOTAL                │ 22       │ 27          │ 13           │
└──────────────────────┴──────────┴─────────────┴──────────────┘

TOTAL GÉNÉRAL : 62 FONCTIONS
```

---

## 🎯 FONCTIONS PAR CATÉGORIE

### 🔄 Navigation (7 fonctions)
- surAllerConnexion()
- surAllerInscription()
- surRetour()
- surRetourConnexion()
- ouvrirConversation()

### 🔐 Authentification (4 fonctions)
- surConnexion()
- surSoumettreInscription()
- surDeconnexion()
- surReconnexion()

### 💬 Messagerie (5 fonctions)
- surEnvoyerMessage()
- onMessageReceived()
- onHistoryReceived()
- afficherConversation()
- notifierNouveauMessage()

### 👥 Gestion Membres (5 fonctions)
- surRafraichirMembres()
- surRechercherMembres()
- onUsersReceived()
- onUserStatus()
- appliquerFiltreMembres()

### 🎨 Interface (8 fonctions)
- initialize() (x4)
- surBasculerTheme() (x4)
- surBasculerPanneauMembres()
- definirMembresReduits()
- appliquerMiseEnPageResponsive()
- appliquerBadgeRole()

### 🔌 Réseau (6 fonctions)
- assurerConnexion() (x2)
- onLoginSuccess() (x2)
- onRegisterSuccess() (x2)
- onError() (x4)
- onInfo() (x4)
- onDisconnected() (x4)
- definirModeHorsLigne()
- tenterReconnexion()

### 🎭 Affichage (4 fonctions)
- configurerListeMembres()
- configurerListeMessages()
- obtenirInitiales()
- secouer() (x2)

---

**📚 Documentation Complète - Version 2.0**
**Total : 62 fonctions documentées**
