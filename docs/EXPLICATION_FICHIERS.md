# Explication des fichiers principaux

## Serveur

- `src/main/java/server/MainServeur.java`
  Point d'entree serveur. Ouvre le `ServerSocket`, accepte les clients et cree un thread par client.

- `src/main/java/server/handler/ClientHandler.java`
  Traite les commandes d'un client connecte (register/login/msg/history/list_users), applique les controles et envoie les reponses protocole.

- `src/main/java/config/JpaUtil.java`
  Cree et fournit l'`EntityManagerFactory` JPA partagee.

## Services metier

- `src/main/java/service/AuthService.java`
  Inscription, authentification BCrypt, controle anti double connexion, liste des utilisateurs visibles selon role.

- `src/main/java/service/MessageService.java`
  Validation et creation des messages, historique, livraison differee, mise a jour des statuts ENVOYE/RECU/LU.

- `src/main/java/service/SessionRegistry.java`
  Registre en memoire des sessions actives (ONLINE), utilisé pour le temps reel.

- `src/main/java/service/ServerServices.java`
  Assembleur/point d'acces des services utilises par `ClientHandler`.

- `src/main/java/service/ClientSession.java`
  Objet session (username, role, writer) pour envoyer des messages vers un client.

## Persistance

- `src/main/java/model/User.java`
  Entite JPA utilisateur (`username`, `password`, `role`, `status`, `dateCreation`).

- `src/main/java/model/Message.java`
  Entite JPA message (expediteur, destinataire, contenu, date, statut).

- `src/main/java/model/Role.java`
  Enum des roles: `ORGANISATEUR`, `MEMBRE`, `BENEVOLE`.

- `src/main/java/model/Status.java`
  Enum statut utilisateur: `ONLINE`, `OFFLINE`.

- `src/main/java/model/MessageStatus.java`
  Enum statut message: `ENVOYE`, `RECU`, `LU`.

- `src/main/java/dao/UserRepository.java`
  DAO utilisateurs: recherche, sauvegarde, update statut, listes triees.

- `src/main/java/dao/MessageRepository.java`
  DAO messages: conversation, messages en attente, statuts, partenaires de discussion.

## Client JavaFX

- `src/main/java/client/MainClient.java`
  Point d'entree JavaFX.

- `src/main/java/client/AppContext.java`
  Etat global client (utilisateur connecte, role, connexion serveur).

- `src/main/java/client/network/ServerConnection.java`
  Socket client, envoi commandes protocole et lecture des evenements serveur.

- `src/main/java/client/network/ServerEventListener.java`
  Interface callback pour reactualiser l'UI selon les evenements reseau.

- `src/main/java/client/controller/LoginController.java`
  Ecran de connexion.

- `src/main/java/client/controller/RegisterController.java`
  Ecran d'inscription.

- `src/main/java/client/controller/WelcomeController.java`
  Ecran d'accueil/navigation.

- `src/main/java/client/controller/MainController.java`
  Ecran principal de chat, liste membres, historique, envoi message.

- `src/main/java/client/model/UserView.java`
  Modele de vue utilisateur pour affichage UI.

- `src/main/java/client/model/ChatMessageView.java`
  Modele de vue message pour affichage conversation.

## Ressources

- `src/main/resources/fxml/login.fxml`, `register.fxml`, `welcome.fxml`, `main.fxml`
  Vues JavaFX (structure des ecrans).

- `src/main/resources/styles/app.css`
  Theme graphique moderne de l'application.

- `src/main/resources/META-INF/persistence.xml`
  Configuration JPA/Hibernate (MySQL par defaut).

- `src/main/resources/logback.xml`
  Configuration logs (console).

## Scripts

- `init-db.sql`
  Script SQL MySQL complet (creation base + tables + contraintes).

- `init-db-postgresql.sql`
  Script PostgreSQL legacy (optionnel).

- `start-server.bat`
  Lance le serveur.

- `start-client.bat`
  Lance le client JavaFX.
