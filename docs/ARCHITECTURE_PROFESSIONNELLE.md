# Architecture Professionnelle - Messagerie Association

## 1) Vue d'ensemble

Application client-serveur en Java:
- Client JavaFX: interface utilisateur, navigation, affichage temps reel
- Serveur Socket: authentification, routage des messages, gestion sessions
- Persistance JPA/Hibernate: stockage users/messages en base MySQL

## 2) Structure actuelle du projet

Le projet est organise selon la structure cible:

- `client`
- `client.controller`
- `client.model`
- `client.network`
- `server`
- `server.handler`
- `model`
- `dao`
- `service`
- `config`
- `util`

## 3) Correspondance avec la structure cible demandee

Structure cible demandee:
- `client/controller/view/network`
- `server/handler/network`
- `model/dao/service/config/util`

Correspondance concrete:
- `client.controller` -> `client/controller`
- `src/main/resources/fxml` -> `client/view`
- `client.network` -> `client/network`
- `server.handler.ClientHandler` -> `server/handler`
- `model` -> `model`
- `dao` -> `dao`
- `service` -> `service`
- `config` -> `config`
- `util.Protocol` -> `util` (protocole partage)

## 4) Flux principal

1. Le client ouvre une socket vers `MainServeur`.
2. `ClientHandler` traite les commandes protocole (`LOGIN`, `REGISTER`, `MSG`, `HISTORY`, `LIST_USERS`).
3. Les services metier appliquent les regles:
   - `AuthService`
   - `MessageService`
   - `SessionRegistry`
4. Les DAO (`UserRepository`, `MessageRepository`) lisent/ecrivent en base via JPA.
5. Le serveur notifie les clients connectes en temps reel.

## 5) Regles metier deja implementees

- Username unique
- Authentification obligatoire pour envoyer
- Une seule session active par utilisateur
- ONLINE/OFFLINE synchronise avec connexion/deconnexion
- Message differe si destinataire offline
- Limite de contenu a 1000 caracteres
- Historique chronologique
- Mot de passe hash (BCrypt)
- Client multithread cote serveur
- Logs connexions/deconnexions/messages
- Vue etendue ORGANISATEUR

## 6) Livrables ajoutes pour completude

- UML classes: `docs/uml-class-diagram.puml`
- SQL MySQL complet: `init-db.sql`
- Script PostgreSQL legacy: `init-db-postgresql.sql`
- Configuration JPA par defaut sur MySQL: `src/main/resources/META-INF/persistence.xml`
