# 🎯 GUIDE VISUEL - TOUTES LES FONCTIONS

## 📱 ÉCRAN PAR ÉCRAN

### 🏠 ÉCRAN D'ACCUEIL (WelcomeController)

```
┌─────────────────────────────────────────────────────────┐
│                  MESSAGERIE INTERNE                     │
│                                                         │
│              [SE CONNECTER]  [S'INSCRIRE]              │
│                                                         │
│                   [Mode sombre]                         │
└─────────────────────────────────────────────────────────┘

FONCTIONS :
├─ surAllerConnexion()      → Charge login.fxml
├─ surAllerInscription()    → Charge register.fxml
└─ surBasculerTheme()       → Change thème clair/sombre
```

---

### 🔐 ÉCRAN DE CONNEXION (LoginController)

```
┌─────────────────────────────────────────────────────────┐
│  CONNEXION                                              │
│  Bienvenue                                              │
│                                                         │
│  Nom d'utilisateur: [____________]                     │
│  Mot de passe:      [____________]                     │
│                                                         │
│  [SE CONNECTER] [S'INSCRIRE] [RETOUR]                 │
│  [Mode sombre]  Message: Serveur: localhost:8088       │
└─────────────────────────────────────────────────────────┘

FONCTIONS :
├─ initialize()              → Affiche message flash ou serveur
├─ surConnexion()            → Valide + envoie LOGIN au serveur
├─ surAllerInscription()     → Charge register.fxml
├─ surRetour()               → Charge welcome.fxml
├─ surBasculerTheme()        → Change thème
├─ assurerConnexion()        → Crée/réutilise connexion serveur
├─ secouer()                 → Animation erreur (400ms)
│
└─ CALLBACKS SERVEUR :
   ├─ onLoginSuccess()       → Charge main.fxml + configure
   ├─ onRegisterSuccess()    → Message flash + login.fxml
   ├─ onError()              → Affiche erreur + secoue
   ├─ onInfo()               → Affiche message
   └─ onDisconnected()       → Affiche raison
```

---

### 📝 ÉCRAN D'INSCRIPTION (RegisterController)

```
┌─────────────────────────────────────────────────────────┐
│  INSCRIPTION                                            │
│  Créer un compte                                        │
│                                                         │
│  Nom d'utilisateur *:    [____________]                │
│  Mot de passe *:         [____________]                │
│  Confirmer mot de passe: [____________]                │
│  Rôle *:                 [MEMBRE ▼]                    │
│                                                         │
│  [VALIDER L'INSCRIPTION] [RETOUR CONNEXION]           │
│  [Mode sombre]  Message: Complétez le formulaire       │
└─────────────────────────────────────────────────────────┘

RÔLES DISPONIBLES :
├─ ORGANISATEUR  → Accès complet + gestion membres
├─ MEMBRE        → Accès standard (par défaut)
└─ BENEVOLE      → Accès standard

FONCTIONS :
├─ initialize()              → Charge rôles + sélectionne MEMBRE
├─ surSoumettreInscription() → Valide + envoie REGISTER
│  ├─ Vérifie champs non vides
│  ├─ Vérifie mots de passe identiques
│  └─ Envoie au serveur
├─ surRetourConnexion()      → Charge login.fxml
├─ surBasculerTheme()        → Change thème
├─ assurerConnexion()        → Crée/réutilise connexion
├─ secouer()                 → Animation erreur
│
└─ CALLBACKS SERVEUR :
   ├─ onRegisterSuccess()    → Message flash + login.fxml
   ├─ onError()              → Affiche erreur + secoue
   │  ├─ USER_EXISTS
   │  ├─ INVALID_USERNAME
   │  ├─ WEAK_PASSWORD
   │  └─ INVALID_ROLE
   ├─ onInfo()               → Affiche message
   └─ onDisconnected()       → Affiche raison
```

---

### 💬 ÉCRAN PRINCIPAL (MainController)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ MESSAGERIE ASSOCIATION                    [EN LIGNE] Jean (MEMBRE)         │
│ [Rafraîchir] [Thème] [Reconnexion] [Déconnexion]                          │
├──────────────┬──────────────────────────────────────┬──────────────────────┤
│ MEMBRES (5)  │ Conversation avec: Marie             │ GESTION (ORGA)      │
│ [Masquer]    │ [En train d'écrire...]               │                     │
│              │                                       │                     │
│ [Chercher..] │ ┌──────────────────────┐             │ Liste membres       │
│              │ │ Bonjour !           │╲            │ pour gestion        │
│ 🟢 Marie     │ └──────────────────────┘ ╲           │                     │
│    MEMBRE    │                           ╲          │                     │
│              │     ╱┌──────────────────┐             │                     │
│ 🟢 Paul      │    ╱ │ Salut !         │             │                     │
│    BENEVOLE  │   ╱  └──────────────────┘             │                     │
│              │                                       │                     │
│ ⚪ Sophie    │ [Écrivez votre message...] [Envoyer] │                     │
│    ORGA      │                                       │                     │
└──────────────┴──────────────────────────────────────┴──────────────────────┘

FONCTIONS PRINCIPALES :

📋 INITIALISATION
├─ initialize()
│  ├─ Affiche identité (nom + rôle)
│  ├─ Applique badge rôle (couleur)
│  ├─ Configure panneau organisateur
│  ├─ Configure listes (membres, messages)
│  ├─ Configure double-clic → conversation
│  ├─ Configure indicateur saisie
│  ├─ Initialise état EN LIGNE
│  └─ Configure responsive

💬 MESSAGERIE
├─ surEnvoyerMessage()
│  ├─ Vérifie destinataire sélectionné
│  ├─ Vérifie connexion active
│  ├─ Vérifie message non vide
│  ├─ Vérifie message < 1000 caractères
│  └─ Envoie au serveur
├─ onMessageReceived()
│  ├─ Si aucune conversation → ouvre avec expéditeur
│  ├─ Si conversation active → affiche
│  └─ Sinon → notification
├─ onHistoryReceived()
│  ├─ Vérifie bon partenaire
│  ├─ Remplace conversation locale
│  └─ Affiche messages
├─ ouvrirConversation()
│  ├─ Définit utilisateur sélectionné
│  ├─ Met à jour titre
│  └─ Demande historique
├─ afficherConversation()
│  ├─ Trie par date
│  ├─ Injecte dans liste
│  └─ Scroll vers dernier
└─ notifierNouveauMessage()
   └─ Ouvre popup Alert

👥 GESTION MEMBRES
├─ surRafraichirMembres()
│  └─ Demande liste au serveur
├─ surRechercherMembres()
│  └─ Filtre localement
├─ onUsersReceived()
│  ├─ Met à jour liste locale
│  ├─ Retire utilisateur courant
│  └─ Applique filtre
├─ onUserStatus()
│  ├─ Affiche notification
│  └─ Rafraîchit liste
└─ appliquerFiltreMembres()
   ├─ Récupère texte recherche
   ├─ Filtre par nom (contient)
   ├─ Met à jour listes
   └─ Met à jour compteur

🔌 CONNEXION
├─ surDeconnexion()
│  ├─ Envoie LOGOUT
│  ├─ Efface identité
│  └─ Retourne accueil
├─ surReconnexion()
│  └─ Lance tentative reconnexion
├─ onDisconnected()
│  ├─ Passe en mode hors ligne
│  └─ Lance reconnexion auto
├─ onLoginSuccess()
│  ├─ Repasse en ligne
│  ├─ Désactive buffer
│  ├─ Demande liste
│  └─ Restaure conversation
├─ definirModeHorsLigne()
│  ├─ Mémorise état
│  ├─ Désactive/active champs
│  ├─ Change statut visuel
│  ├─ Affiche/masque bannière
│  └─ Affiche/masque bouton
└─ tenterReconnexion()
   ├─ Attend 3s si auto
   ├─ Récupère identifiants
   ├─ Crée nouvelle connexion
   ├─ Envoie LOGIN
   └─ Met à jour AppContext

🎨 INTERFACE
├─ surBasculerPanneauMembres()
│  └─ Bascule réduit/étendu
├─ surBasculerTheme()
│  └─ Change thème
├─ definirMembresReduits()
│  ├─ Affiche/masque éléments
│  ├─ Ajuste largeur (280↔88px)
│  └─ Change texte bouton
├─ appliquerMiseEnPageResponsive()
│  ├─ < 760px : Mode mobile
│  ├─ 760-1100px : Mode tablette
│  ├─ > 1100px : Mode desktop
│  ├─ Applique classe CSS
│  ├─ Ajuste panneau orga
│  └─ Ajuste largeur membres
├─ appliquerBadgeRole()
│  ├─ Affiche texte rôle
│  └─ Applique classe CSS
│     ├─ role-organisateur (doré)
│     ├─ role-membre (bleu)
│     └─ role-benevole (vert)
├─ configurerListeMembres()
│  └─ Crée cellule avec :
│     ├─ Avatar (initiales)
│     ├─ Nom
│     ├─ Rôle
│     └─ Statut (🟢/⚪)
├─ configurerListeMessages()
│  └─ Crée bulle avec :
│     ├─ Contenu
│     ├─ Métadonnées (qui + quand)
│     ├─ Style (bleu/gris)
│     └─ Alignement (droite/gauche)
└─ obtenirInitiales()
   ├─ Si composé → 2 premières lettres
   └─ Sinon → 2 premiers caractères
```

---

## 🔄 FLUX DE NAVIGATION

```
┌─────────────┐
│  WELCOME    │ ← Point d'entrée
└──────┬──────┘
       │
       ├─→ [SE CONNECTER] ──→ ┌─────────────┐
       │                      │   LOGIN     │
       │                      └──────┬──────┘
       │                             │
       │                             ├─→ [Connexion réussie]
       │                             │   └─→ ┌─────────────┐
       │                             │       │    MAIN     │
       │                             │       └─────────────┘
       │                             │
       │                             └─→ [RETOUR] ──┐
       │                                            │
       └─→ [S'INSCRIRE] ──→ ┌─────────────┐        │
                            │  REGISTER   │        │
                            └──────┬──────┘        │
                                   │               │
                                   ├─→ [Inscription réussie]
                                   │   └─→ LOGIN (avec message)
                                   │
                                   └─→ [RETOUR CONNEXION] ─┘
```

---

## 📊 STATISTIQUES COMPLÈTES

```
┌────────────────────────────────────────────────────────────┐
│  CONTRÔLEUR          │ MÉTHODES │ CALLBACKS │ UTILITAIRES │
├──────────────────────┼──────────┼───────────┼─────────────┤
│  WelcomeController   │    3     │     0     │      0      │
│  LoginController     │    5     │     9     │      2      │
│  RegisterController  │    4     │     9     │      2      │
│  MainController      │   10     │     9     │      9      │
├──────────────────────┼──────────┼───────────┼─────────────┤
│  TOTAL               │   22     │    27     │     13      │
└────────────────────────────────────────────────────────────┘

TOTAL GÉNÉRAL : 62 FONCTIONS DOCUMENTÉES
```

---

## 🎯 FONCTIONS PAR CATÉGORIE

### 🔄 NAVIGATION (7)
```
surAllerConnexion()         → welcome → login
surAllerInscription()       → welcome/login → register
surRetour()                 → login → welcome
surRetourConnexion()        → register → login
ouvrirConversation()        → Ouvre chat avec membre
```

### 🔐 AUTHENTIFICATION (4)
```
surConnexion()              → Envoie LOGIN
surSoumettreInscription()   → Envoie REGISTER
surDeconnexion()            → Envoie LOGOUT + retour accueil
surReconnexion()            → Reconnexion manuelle
```

### 💬 MESSAGERIE (5)
```
surEnvoyerMessage()         → Envoie message
onMessageReceived()         → Reçoit message
onHistoryReceived()         → Reçoit historique
afficherConversation()      → Affiche messages
notifierNouveauMessage()    → Popup notification
```

### 👥 GESTION MEMBRES (5)
```
surRafraichirMembres()      → Demande liste
surRechercherMembres()      → Filtre local
onUsersReceived()           → Reçoit liste
onUserStatus()              → Reçoit changement statut
appliquerFiltreMembres()    → Applique filtre
```

### 🎨 INTERFACE (8)
```
initialize() x4             → Init écrans
surBasculerTheme() x4       → Change thème
surBasculerPanneauMembres() → Affiche/masque panneau
definirMembresReduits()     → Réduit/étend panneau
appliquerMiseEnPageResponsive() → Adapte taille
appliquerBadgeRole()        → Style badge rôle
```

### 🔌 RÉSEAU (14)
```
assurerConnexion() x2       → Crée/réutilise connexion
onLoginSuccess() x2         → Callback connexion OK
onRegisterSuccess() x2      → Callback inscription OK
onError() x4                → Callback erreur
onInfo() x4                 → Callback info
onDisconnected() x4         → Callback déconnexion
definirModeHorsLigne()      → Gère état hors ligne
tenterReconnexion()         → Reconnexion auto/manuelle
```

### 🎭 AFFICHAGE (4)
```
configurerListeMembres()    → Cellules membres
configurerListeMessages()   → Bulles messages
obtenirInitiales()          → Initiales avatar
secouer() x2                → Animation erreur
```

---

## 🚀 UTILISATION TYPIQUE

### Scénario 1 : Première Connexion
```
1. welcome.surAllerInscription()
2. register.initialize()
3. register.surSoumettreInscription()
4. register.onRegisterSuccess()
5. login.initialize() (avec message flash)
6. login.surConnexion()
7. login.onLoginSuccess()
8. main.initialize()
```

### Scénario 2 : Envoi de Message
```
1. main.ouvrirConversation(marie)
2. main.onHistoryReceived()
3. main.afficherConversation()
4. [Utilisateur tape message]
5. main.surEnvoyerMessage()
6. [Serveur traite]
7. main.onMessageReceived()
8. main.afficherConversation()
```

### Scénario 3 : Reconnexion
```
1. [Serveur se déconnecte]
2. main.onDisconnected()
3. main.definirModeHorsLigne(true)
4. main.tenterReconnexion(auto=true)
5. [Attend 3 secondes]
6. [Crée nouvelle connexion]
7. main.onLoginSuccess()
8. main.definirModeHorsLigne(false)
```

---

**📚 Guide Visuel Complet - Version 2.0**
**62 fonctions • 4 contrôleurs • 100% documenté**
